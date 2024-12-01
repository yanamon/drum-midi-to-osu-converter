package model.midi.percussion;

public enum Category {

    Crash(0),
    HighHat(1),
    Snare(2),

    Kick(3),

    Ride(4),
    Tom(5),
    Floor(6);

    private int osuManiaColumn;

    Category(int osuManiaColumn) {
        this.osuManiaColumn = osuManiaColumn;
    }

    public int getOsuManiaColumn() {
        return this.osuManiaColumn;
    }

}
