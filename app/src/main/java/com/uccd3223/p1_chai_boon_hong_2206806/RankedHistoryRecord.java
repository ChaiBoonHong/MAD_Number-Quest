package com.uccd3223.p1_chai_boon_hong_2206806;

public final class RankedHistoryRecord {
    private final HistoryRecord record;
    private final boolean highest;

    public RankedHistoryRecord(HistoryRecord record, boolean highest) {
        this.record = record;
        this.highest = highest;
    }

    public HistoryRecord getRecord() { return record; }
    public boolean isHighest() { return highest; }
}
