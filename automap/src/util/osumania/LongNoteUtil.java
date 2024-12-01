package util.osumania;

public final class LongNoteUtil {

    public static int getMiniLnDuration(int bpm) {
        return (int) Math.floor((60000 / (double) bpm) * (1.0 / 16.0));
    }

}
