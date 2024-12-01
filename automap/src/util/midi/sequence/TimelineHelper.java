package util.midi.sequence;

import util.Utils;
import javax.sound.midi.*;
import java.util.ArrayList;

public final class TimelineHelper {

    public static long getFirstTick(Sequencer sequencer) {
        long tick = Long.MAX_VALUE;
        for (Track track : sequencer.getSequence().getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                long currentTick = event.getTick();
                if (currentTick < tick) {
                    tick = currentTick;
                }
                break;
            }
        }
        return tick;
    }

    public static long getAbsFromTime(long time, ArrayList<Long> tickTimeline, ArrayList<Long> absTimeline,
                                      int resolution, ArrayList<Long> tempoArray) {
        int index = Utils.getIndexFromTimeline(time,
            tickTimeline);
        long tickDuration = time - tickTimeline.get(index);
        long startT = absTimeline.get(index);
        return  startT + Utils.tickToMilliSec(tickDuration, resolution, tempoArray.get(index));
    }

}
