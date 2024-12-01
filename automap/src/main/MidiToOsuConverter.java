package main;

import gui.WindowProgress;
import model.Note;
import model.NoteArray;
import model.OsuBeatmap;
import model.Timing;
import model.midi.percussion.DrumSample;
import util.*;
import util.common.BpmToTempoConverter;
import util.midi.sequence.TimelineHelper;

import javax.sound.midi.*;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


public class MidiToOsuConverter implements Runnable {
    private final int NOTE_ON = 0x90;
    private final int NOTE_OFF = 0x80;
    private final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#",
            "G", "G#", "A", "A#", "B"};
    private final String version;
    TreeMap<String, String> hsHM = new TreeMap<String, String>();
    // tempo
    ArrayList<Long> absTimeline = new ArrayList<Long>();
    ArrayList<Long> tickTimeline = new ArrayList<Long>();
    ArrayList<Long> tempoArray = new ArrayList<Long>();
    //instrument
    int[] instruments = new int[16];
    // User Input
    private String filename;
    private boolean extractNotes;
    private int keyCount;
    // Need implement LN
    private int LN_Cutoff = 999999;
    private int volume = 100;
    private boolean customHS = false;
    private String convert;
    private int DifficultyOfDensity = 7;
    private String artist = "Unknown";
    private int MAX_CHORD = 3;
    private int sampleRate = 16000;
    private int bitDepth = 16;
    private int channelMode = 2;
    private boolean customTiming = false;
    private boolean mergeHS;
    // Need fix OGG
    private boolean convertOGG = false;
    // Constants
    private int bpm;
    private String hitsoundPath;
    private String outputPath;
    private Sequencer sequencer = null;
    private int HitSoundSize = 0;
    private int outputSize = 2;
    private int[] options;
    private WindowProgress progressWindow = null;
    private String midiPath;
    private long songDurationInMS = 0;
    private int currentSize = 0;

    // Constructor
    public MidiToOsuConverter(Sequencer seq, String name,
                              Boolean extractNotes, int keys, int maxChord, int OD, int[] trackOptions, boolean mergeHitSound, boolean customHitSound) {

        readFromProperty();
        if (seq == null) {
            throw new IllegalArgumentException("Null Sequencer");
        }
        //System.out.println(seq.getMicrosecondLength());
        sequencer = seq;
        this.extractNotes = extractNotes;
        keyCount = keys;
        MAX_CHORD = maxChord;
        DifficultyOfDensity = OD;
        filename = name;
        options = trackOptions;
        version = "KS" + DifficultyOfDensity + "-" + keyCount + "K";
        hitsoundPath = midiPath + "\\"
                + filename.substring(0, filename.length() - 4) + "_hitsounds\\";
        outputPath = midiPath + "\\" + filename.substring(0, filename.length() - 4)
                + "_outputs\\";
        mergeHS = mergeHitSound;
        customHS = customHitSound;
        convert = PropertyAdapter.PATH + "\\convert.csv";
    }

    private void readFromProperty() {
        midiPath = PropertyAdapter.readFromProperty(PropertyAdapter.MIDI_PATH);
    }

    public void run() {
        songDurationInMS = sequencer.getMicrosecondLength() / 1000L;
        Utils.createFolder(outputPath);
        if (songDurationInMS < 0) {
            JOptionPane.showMessageDialog(null, "Error with Midi file, no empty mp3 created!");
        }
        try {
            if (customTiming) {
                long[] timings = {0L};
                double[] bpms = {124.5};
                String file = PropertyAdapter.PATH + "\\customBPM.mid";
                sequencer = MidiUtils.emptyTempos(sequencer);
                MidiUtils.setTempos(sequencer, timings, bpms);
                //util.MidiUtils.keepOnlyTrack(sequencer, 4);
                MidiUtils.saveMidi(sequencer, file);
            }
            loadTimeline(sequencer);
            ArrayList<NoteArray> info = getMidiInfo(sequencer, options);
            toOsuBeatmap(info);
            copyDrumSamples();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // display finished
        JOptionPane.showMessageDialog(null, "Finished!");
    }

    private void loadTimeline(Sequencer sequencer) throws Exception {
        if (tempoArray.size() >= 1) {
            return;
        }
        if (sequencer.getSequence().getDivisionType() != Sequence.PPQ) {
            System.out.println("Division Type = " + sequencer.getSequence().getDivisionType());
            throw new Exception("Unsupported for now");
        }
        // resolution is ticks per beat, 1 beat is 1 quarter note
        int res = sequencer.getSequence().getResolution();

        for (int i = 0; i < 16; i++) {
            instruments[i] = 0;
        }
        // default tempo is 120 bpm = 2 beat per sec = 1 beat is 500ms =
        // 500000us per beat
        long tempo = 500000;
        // read all bpm changes and store it into absolute and tick time lines
        for (Track track : sequencer.getSequence().getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                long t = event.getTick();
                MidiMessage message = event.getMessage();

                if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    if (mm.getType() == 81) {
                        // tempo message
                        byte[] data = mm.getData();
                        // tempo is duration of quarter note in micro sec
                        tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8
                                | (data[2] & 0xff);
                        //System.out.println("tempo event at t = " + t + " is " + tempo);

                        if (tempoArray.size() == 0) {
                            tempoArray.add(tempo);
                            tickTimeline.add(t);
                            absTimeline.add(Utils.tickToMilliSec(t, res, 120));
                        } else {
                            int index = tickTimeline.size() - 1;
                            long tickDuration = t - tickTimeline.get(index);
                            long absTime = absTimeline.get(index)
                                    + Utils.tickToMilliSec(tickDuration, res,
                                    tempoArray.get(index));
                            if (!absTimeline.contains(absTime)) {
                                // store new tempo
                                tempoArray.add(tempo);
                                // store time into tick time line
                                tickTimeline.add(t);
                                // store time into absolute time line
                                absTimeline.add(absTime);
                            } else {
                                tempoArray.remove(tempoArray.size() - 1);
                                tempoArray.add(tempo);
                            }
                        }


                    }

                } else if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int status = sm.getStatus();
                    if (status >= 0xC0 && status <= 0xCF) {
                        // Program change (instrument)
                        instruments[sm.getChannel()] = sm.getData1();
                    }
                }

            }// end of track

        }

        int firstBpm = (int) sequencer.getTempoInBPM();
        if (tempoArray.size() == 0) {
            if (firstBpm != 0) {
                tempoArray.add(BpmToTempoConverter.bpmToTempo(firstBpm));
                long firstTick = TimelineHelper.getFirstTick(sequencer);
                tickTimeline.add(firstTick);
                absTimeline.add(Utils.tickToMilliSec(firstTick, res, 120));
            } else {
                tempoArray.add(500000L);
                tickTimeline.add(0l);
                absTimeline.add(0l);
            }
        }

        //System.out.println(tempoArray);
    }

    /**
     * @param sequencer
     * @return an ArrayList containing first the total notes, and then all the notes to extract as hitsounds
     * @throws Exception
     */
    public ArrayList<NoteArray> getMidiInfo(Sequencer sequencer, int[] options) throws Exception {
        ArrayList<NoteArray> output = new ArrayList<>();
        long time = 0;
        int resolution = this.sequencer.getSequence().getResolution();
        // list of unique notes for all tracks
        NoteArray allTrackUniqueNotes = new NoteArray();
        // List of all notes
        NoteArray totalNotes = new NoteArray();
        NoteArray BGNotes = new NoteArray();
        int trackID = 0;
        for (Track track : sequencer.getSequence().getTracks()) { // for each track
            int toBG = options[trackID];
            if (toBG >= 0) {
                NoteArray notes = new NoteArray(); // temporary array of
                int instrument = -1;
                int channelVolume = 100;
                for (int i = 0; i < track.size(); i++) {
                    String line = "";
                    MidiEvent event = track.get(i);
                    time = event.getTick();
                    line = line + "@" + time + " ";

                    MidiMessage message = event.getMessage();
                    if (message instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) message;
                        bpm = Utils.getBpm(tickTimeline, tempoArray, time);

                        // NOTE ON
                        if (sm.getCommand() >= 0x90 && sm.getCommand() <= 0x9F) {
                            int channel = sm.getChannel();
                            instrument = instruments[channel];
                            int key = sm.getData1();
                            int octave = (key / 12) - 2;
                            int note = key % 12;
                            String noteName = NOTE_NAMES[note];
                            int velocity = sm.getData2();

                            if (key == 58) {
                                System.out.println("ON at " + time);
                            }

                            Note n = null;
                            long absT = -1;
                            if (velocity > 0) {
                                // Note ON
                                // resolution = tick per beat
                                // bpm = beat per min
                                int index = Utils.getIndexFromTimeline(time,
                                        tickTimeline);
                                long tickDuration = time - tickTimeline.get(index);
                                long startT = absTimeline.get(index);
                                absT = startT
                                        + Utils.tickToMilliSec(tickDuration, resolution,
                                        tempoArray.get(index));
                                if (absT < 0) {
                                    System.out.println("tick time = " + time);
                                    System.out.println("absolute time = " + absT);
                                    System.out.println("tickDuration duration = "
                                            + tickDuration);
                                    throw new IllegalArgumentException();
                                }

                                if (octave < 0) {
                                    octave = Math.abs(octave);
                                    n = new Note(noteName + "_" + octave, velocity,
                                            time, key, bpm, absT, instrument,
                                            channelVolume);
                                } else {
                                    n = new Note(noteName + octave, velocity, time,
                                            key, bpm, absT, instrument,
                                            channelVolume);
                                }
                                n.setChannel(channel);
                                notes.add(n);
                                totalNotes.add(n);
                                line = line + "Note on, " + noteName + octave
                                        + " velocity: " + velocity + "\n";
                            }
                        }
                    }

                }// end of 1 track

            }// end of if (toBG>= 0)
            trackID++;
        }// end of tracks


        // osu hit objects
        output.add(totalNotes);
        // hit sounds
        output.add(allTrackUniqueNotes);
        // BG Notes
        output.add(BGNotes);
        return output;
    }

    public void toOsuBeatmap(ArrayList<NoteArray> input) throws MidiUnavailableException, InvalidMidiDataException, IOException {
        // Create empty WAV file with same duration as song
        currentSize++;
        NoteArray totalNotes = input.get(0);
        if (totalNotes.getSize() == 0) {
            System.out.println("No notes to convert!");
        }
        NoteArray allTrackUniqueNotes = input.get(1);
        NoteArray BGNotes = input.get(2);
        //Timing Section
        String timingSections = "";
        for (int i = 0; i < absTimeline.size(); i++) {
            Timing tp = new Timing(absTimeline.get(i), Utils.tempoToMilliSec(tempoArray.get(i)));
            timingSections += tp.toOsuTimingPoint();
        }

        if (extractNotes) {
            if (!Utils.isFolderEmpty(hitsoundPath)) {
                int reply = JOptionPane
                        .showConfirmDialog(
                                null,
                                "Do you wish to replace existing files within hit sound folder?",
                                "Midi to Osu Converter",
                                JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    Utils.emptyFolder(hitsoundPath);
                } else {
                    System.exit(0);
                }
            }
            HitSoundSize = allTrackUniqueNotes.getSize();
        } else {
            HitSoundSize = 0;
        }
        progressWindow = new WindowProgress();
        progressWindow.setProgressMax(this.HitSoundSize + this.outputSize);
        progressWindow.display("total number of notes = " + totalNotes.getSize());
        ArrayList<NoteArray> list = totalNotes.sortNotesByTime();
        System.out.println("Sorted notes into chords...");
        DrumNoteProcessor ChartConv = new DrumNoteProcessor();
        ChartConv.process(list);
        System.out.println("Sent some set notes into SB...");
        String osuOutput = "";
        String sampleOutput = "//Storyboard Sound Samples\n"; // all the bg


        Iterator<NoteArray> ite = list.iterator();
        while (ite.hasNext()) {
            //System.out.println("Treating chord "+loop+ "  ...");
            NoteArray chord = ite.next();
            sampleOutput += chord.toBackgroundSample(volume);
            osuOutput += chord.toHitObjects(keyCount,
                    sequencer.getSequence().getResolution(), LN_Cutoff, volume);
        }
        System.out.println("Finished map...");
        OsuBeatmap beatmap = new OsuBeatmap(
                Utils.getFilenameWithoutExtensionFromPath(filename),
                sampleOutput, osuOutput);
        beatmap.setTimingPoints(timingSections);
        beatmap.setArtist(artist);
        beatmap.setVersion(version);
        beatmap.setKeyCount(keyCount);
        Utils.writeToFile(progressWindow,
                outputPath + artist + " - "
                        + Utils.getFilenameWithoutExtensionFromPath(filename)
                        + " (Automap-chan) [" + version + "].osu",
                beatmap.toString());
        progressWindow.display("finished outputing osu hit objects!");
        currentSize++;
        progressWindow.updateProgress(currentSize);
    }

    private void copyDrumSamples() {
        Arrays.stream(DrumSample.values()).forEach(this::copyDrumSample);
        progressWindow.display("Finished extracting all hit sounds");
    }

    private void copyDrumSample(DrumSample drumSample) {
        String fileName = drumSample.getFilename();
        String sourcePath = DrumSample.getFolderName() + fileName;
        String targetPathString = outputPath + drumSample.getFilename();
        Path targetPath = Paths.get(targetPathString);

        try (InputStream inputStream = MidiToOsuConverter.class.getClassLoader().getResourceAsStream(sourcePath)) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}