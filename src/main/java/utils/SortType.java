package utils;

public enum SortType {
    UNSORTED(0),
    ALPHABETICAL(1),
    SIZE(2),
    DATE(3);

    public final int value;
    SortType(int value) {
        this.value = value;
    }
}
