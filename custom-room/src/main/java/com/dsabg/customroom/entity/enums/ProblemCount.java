package com.dsabg.customroom.entity.enums;

public enum ProblemCount {

    ONE(1),
    THREE(3),
    FIVE(5),
    SEVEN(7),
    NINE(9),
    ELEVEN(11),
    THIRTEEN(13),
    FIFTEEN(15);

    private final int value;

    ProblemCount(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValid(int value) {
        for (ProblemCount count : values()) {
            if (count.value == value) {
                return true;
            }
        }
        return false;
    }
}