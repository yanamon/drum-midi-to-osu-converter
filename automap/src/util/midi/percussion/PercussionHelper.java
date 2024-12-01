package util.midi.percussion;

import model.midi.percussion.Percussion;
import java.util.Arrays;

public class PercussionHelper {

    public static Percussion getPercussionByMidiKey(int midiKey) {
        return Arrays.stream(Percussion.values())
            .filter(percussion -> percussion.getMidiKey() == midiKey)
            .findFirst()
            .orElse(null);
    }

}
