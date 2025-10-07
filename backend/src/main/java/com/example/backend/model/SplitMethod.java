package com.example.backend.model;

public enum SplitMethod {
    equal,
    unequal,
    by_tag;

    public static SplitMethod fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Split method cannot be null");
        }
        for (SplitMethod sm : SplitMethod.values()) {
            if (sm.name().equalsIgnoreCase(value)) {
                return sm;
            }
        }
        throw new IllegalArgumentException("Invalid split method: " + value);
    }
}
