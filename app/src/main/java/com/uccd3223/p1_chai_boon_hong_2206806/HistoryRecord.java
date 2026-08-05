package com.uccd3223.p1_chai_boon_hong_2206806;

import java.util.Objects;

public final class HistoryRecord {
    private final String gameName;
    private final String mode;
    private final int score;
    private final long timestamp;
    private final long timeLimitMs;

    public HistoryRecord(String gameName, String mode, int score, long timestamp) {
        this(gameName, mode, score, timestamp, 0L);
    }

    public HistoryRecord(String gameName, String mode, int score, long timestamp, long timeLimitMs) {
        this.gameName = Objects.requireNonNull(gameName);
        this.mode = Objects.requireNonNull(mode);
        this.score = score;
        this.timestamp = timestamp;
        this.timeLimitMs = timeLimitMs;
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

    public long getTimeLimitMs() { return timeLimitMs; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HistoryRecord)) return false;
        HistoryRecord record = (HistoryRecord) other;
        return score == record.score
                && timestamp == record.timestamp
                && timeLimitMs == record.timeLimitMs
                && gameName.equals(record.gameName)
                && mode.equals(record.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameName, mode, score, timestamp, timeLimitMs);
    }
}
