package part2.actors.utility;

public enum StringEnum {
    SYSTEM,
    ACTOR,
    IMAGE1,
    IMAGE2;

    @Override
    public String toString() {
        if (this == StringEnum.SYSTEM) {
            return "PuzzleCluster";
        } else if (this == StringEnum.ACTOR) {
            return "PuzzleResolver";
        } else if (this == IMAGE1) {
            return "src/main/resources/images/bletchley-park-mansion.jpg";
        } else if (this == IMAGE2) {
            return "src/main/resources/images/colosseum.jpg";
        }
        throw new IllegalCallerException();
    }
}
