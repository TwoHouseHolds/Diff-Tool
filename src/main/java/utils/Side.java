package utils;

public enum Side {
    LEFT, RIGHT;

    @Override
    public String toString(){
        if (this.equals(Side.LEFT)) return "L";
        else return "R";
    }
}
