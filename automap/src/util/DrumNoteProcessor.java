package util;

import model.Note;
import model.NoteArray;
import model.midi.percussion.DrumSample;
import model.midi.percussion.Percussion;
import util.osumania.LongNoteUtil;

import java.util.*;

public class DrumNoteProcessor {

	public void process(ArrayList<NoteArray> chords) {
		chords.forEach(this::setDrumColumn);

		chords.forEach(chord -> chord.getNotes().forEach(note -> {
			setLongNote(note);
			setHitSample(note);
		}));
	}

	private void setDrumColumn(NoteArray chord) {
		Set<Integer> occupiedOsuManiaColumns = new HashSet<>();
		List<Note> notesToRemove = new ArrayList<>();

		for (Note note : chord.getNotes()) {
			Percussion percussion = note.getPercussion();
			if (percussion == null || percussion.getCategory() == null) {
				notesToRemove.add(note);
				continue;
			}

			Integer osuManiaColumn = percussion.getCategory().getOsuManiaColumn();

			if (occupiedOsuManiaColumns.contains(osuManiaColumn)) {
				notesToRemove.add(note);
				continue;
			}

			note.setColumn(osuManiaColumn);
			occupiedOsuManiaColumns.add(osuManiaColumn);
		}

		notesToRemove.forEach(note -> chord.getNotes().remove(note));
	}

	private void setLongNote(Note note) {
		if (note.getPercussion().isLongNote()) {
			note.setLNduration(LongNoteUtil.getMiniLnDuration(note.getBPM()));
		} else {
			note.setLNduration(0);
		}
	}

	private void setHitSample(Note note) {
		String hitSample = Optional.ofNullable(note.getPercussion())
			.map(Percussion::getDrumSample)
			.map(DrumSample::getFilename)
			.orElse("");

		note.setHitSound(hitSample);
	}

}
