package com.uccd3223.p1_chai_boon_hong_2206806;

import java.util.Objects;

public final class AnswerChoice {
    private final int value;
    private final String label;
    private final String contentDescription;

    public AnswerChoice(int value, String label, String contentDescription) {
        this.value = value;
        this.label = Objects.requireNonNull(label);
        this.contentDescription = Objects.requireNonNull(contentDescription);
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getContentDescription() {
        return contentDescription;
    }
}
