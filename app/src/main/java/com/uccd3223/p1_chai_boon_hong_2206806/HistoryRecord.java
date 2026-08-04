package com.uccd3223.p1_chai_boon_hong_2206806;

import java.util.Objects;

public final class HistoryRecord {
    private final String gameName;
    private final String mode;
    private final int score;
    private final long timestamp;

    public HistoryRecord(String gameName, String mode, int score, long timestamp) {
        this.gameName = Objects.requireNonNull(gameName);
        this.mode = Objects.requireNonNull(mode);
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getGameName() {
        return gameName;
    }

    public String getMode() {
        return mode;
    }

    public int getScore() {
        return score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HistoryRecord)) return false;
        HistoryRecord record = (HistoryRecord) other;
        return score == record.score
                && timestamp == record.timestamp
                && gameName.equals(record.gameName)
                && mode.equals(record.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameName, mode, score, timestamp);
    }
}
