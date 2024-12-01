package util.midi.sequence;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public final class ChannelGetter {

    public static int getChannel(Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage msg = event.getMessage();
            if (msg instanceof ShortMessage){
                ShortMessage sm = (ShortMessage) msg;
                int status = sm.getStatus();
                if (status >= 0xC0 && status <= 0xCF) {
                    return sm.getChannel();
                }
            }
        }
        return -1;
    }

}
