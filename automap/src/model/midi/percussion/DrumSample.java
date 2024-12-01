package model.midi.percussion;

public enum DrumSample {

    CRASH("crash.ogg"),
    SPLASH("splash.ogg"),

    CLOSE_HI_HAT("close-hi-hat.ogg"),
    OPEN_HI_HAT("open-hi-hat.ogg"),

    SNARE("snare.ogg"),
    SIDE_STICK("side-stick.ogg"),

    KICK("kick.ogg"),

    RIDE("ride.ogg"),
    BELL("bell.ogg"),

    TOM("tom.ogg"),
    FLOOR("floor.ogg");

    private String filename;

    DrumSample(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    public static String getFolderName() {
        return "drum-sample/";
    }

}
