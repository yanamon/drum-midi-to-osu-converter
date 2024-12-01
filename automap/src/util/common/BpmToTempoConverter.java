package util.common;

public final class BpmToTempoConverter {

    public static long bpmToTempo(int bpm) {
        return (60 * 1000000) / bpm;
    }

}
