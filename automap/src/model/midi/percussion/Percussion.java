package model.midi.percussion;

import java.util.EnumSet;

public enum Percussion {

    CrashCymbal1(49, Category.Crash, DrumSample.CRASH),
    CrashCymbal2(57, Category.Crash, DrumSample.CRASH),
    ChineseCymbal(52, Category.Crash, DrumSample.SPLASH, true),
    SplashCymbal(55, Category.Crash, DrumSample.SPLASH, true),
    Vibraslap(58, Category.Crash, DrumSample.SPLASH, true),

    ClosedHighHat(42, Category.HighHat, DrumSample.CLOSE_HI_HAT),
    OpenHighHat(46, Category.HighHat, DrumSample.OPEN_HI_HAT, true),

    SideStick(37, Category.Snare, DrumSample.SIDE_STICK, true),
    Cowbell(56, Category.Snare, DrumSample.SIDE_STICK, true),
    AcousticSnare(38, Category.Snare, DrumSample.SNARE),
    HandClap(39, Category.Snare, DrumSample.SNARE),
    ElectricSnare(40, Category.Snare, DrumSample.SNARE),

    AcousticBassDrum(35, Category.Kick, DrumSample.KICK),
    BassDrum1(36, Category.Kick, DrumSample.KICK),

    RideCymbal1(51, Category.Ride, DrumSample.RIDE),
    RideCymbal2(59, Category.Ride, DrumSample.RIDE),
    RideBell(53, Category.Ride, DrumSample.BELL, true),
    Tambourine(54, Category.Ride, DrumSample.BELL, true),

    LowMidTom(47, Category.Tom, DrumSample.TOM),
    HighMidTom(48, Category.Tom, DrumSample.TOM),
    HighTom(50, Category.Tom, DrumSample.TOM),

    LowFloorTom(41, Category.Floor, DrumSample.FLOOR),
    HighFloorTom(43, Category.Floor, DrumSample.FLOOR),
    LowTom(45, Category.Floor, DrumSample.FLOOR),

    PedalHighHat(44),
    HighBongo(60),
    LowBongo(61),
    MuteHighConga(62),
    OpenHighConga(63),
    LowConga(64),
    HighTimbale(65),
    LowTimbale(66),
    HighAgogo(67),
    LowAgogo(68),
    Cabasa(69),
    Maracas(70),
    ShortWhistle(71),
    LongWhistle(72),
    ShortGuiro(73),
    LongGuiro(74),
    Claves(75),
    HighWoodBlock(76),
    LowWoodBlock(77),
    MuteCuica(78),
    OpenCuica(79),
    MuteTriangle(80),
    OpenTriangle(81);

    private final int midiKey;

    private boolean isLongNote = false;

    private Category category;

    private DrumSample drumSample;

    Percussion(int midiKey, Category category, DrumSample drumSample) {
        this.midiKey = midiKey;
        this.category = category;
        this.drumSample = drumSample;
    }



    Percussion(int midiKey, Category category, DrumSample drumSample, boolean isLongNote) {
        this.midiKey = midiKey;
        this.category = category;
        this.drumSample = drumSample;
        this.isLongNote = isLongNote;
    }

    Percussion(int midiKey) {
        this.midiKey = midiKey;
    }

    public int getMidiKey() {
        return this.midiKey;
    }

    public Category getCategory() {
        return this.category;
    }

    public boolean isLongNote() {
        return isLongNote;
    }

    public DrumSample getDrumSample() {
        return this.drumSample;
    }

}
