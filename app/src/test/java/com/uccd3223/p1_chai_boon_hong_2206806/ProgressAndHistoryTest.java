package com.uccd3223.p1_chai_boon_hong_2206806;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ProgressAndHistoryTest {
    @Test public void scoreCategoriesSeparateEveryFairMode() {
        assertEquals("FUN", ProgressManager.category("FUN", 0));
        assertEquals("TA_60000", ProgressManager.category("TIME_ATTACK", 60_000));
        assertEquals("TA_90000", ProgressManager.category("TIME_ATTACK", 90_000));
        assertEquals("TA_120000", ProgressManager.category("TIME_ATTACK", 120_000));
        assertEquals("ROUND_RUSH", ProgressManager.category("SCORE_ATTACK", 0));
    }

    @Test public void newestTiedHighestRecordWinsOncePerCategory() {
        HistoryRecord newestTie = new HistoryRecord("Sequence", "TIME_ATTACK", 12, 300, 60_000);
        HistoryRecord olderTie = new HistoryRecord("Sequence", "TIME_ATTACK", 12, 200, 60_000);
        HistoryRecord otherDuration = new HistoryRecord("Sequence", "TIME_ATTACK", 8, 100, 90_000);
        List<RankedHistoryRecord> ranked = HistoryRankingUtil.rankNewestFirst(
                Arrays.asList(newestTie, olderTie, otherDuration));
        assertTrue(ranked.get(0).isHighest());
        assertFalse(ranked.get(1).isHighest());
        assertTrue(ranked.get(2).isHighest());
    }

    @Test public void legacyTimeAttackRecordsNeverReceiveGold() {
        HistoryRecord legacy = new HistoryRecord("Association", "TIME_ATTACK", 99, 200);
        HistoryRecord current = new HistoryRecord("Association", "TIME_ATTACK", 4, 100, 60_000);
        List<RankedHistoryRecord> ranked = HistoryRankingUtil.rankNewestFirst(Arrays.asList(legacy, current));
        assertFalse(ranked.get(0).isHighest());
        assertTrue(ranked.get(1).isHighest());
        assertEquals(0L, legacy.getTimeLimitMs());
    }

    @Test public void gamesAndModesHaveIndependentTrophies() {
        List<RankedHistoryRecord> ranked = HistoryRankingUtil.rankNewestFirst(Arrays.asList(
                new HistoryRecord("Sequence", "FUN", 5, 4),
                new HistoryRecord("Association", "FUN", 3, 3),
                new HistoryRecord("Sequence", "SCORE_ATTACK", 2, 2),
                new HistoryRecord("Sequence", "FUN", 4, 1)));
        assertTrue(ranked.get(0).isHighest());
        assertTrue(ranked.get(1).isHighest());
        assertTrue(ranked.get(2).isHighest());
        assertFalse(ranked.get(3).isHighest());
    }

    @Test public void zeroPointSessionCanExitWithoutConfirmation() {
        assertFalse(BaseGameActivity.shouldConfirmExit(0));
        assertTrue(BaseGameActivity.shouldConfirmExit(1));
    }
}
