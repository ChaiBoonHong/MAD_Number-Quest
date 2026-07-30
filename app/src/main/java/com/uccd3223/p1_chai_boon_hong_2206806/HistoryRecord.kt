package com.uccd3223.p1_chai_boon_hong_2206806

data class HistoryRecord(
    val gameName: String,
    val mode: String, // "FUN", "TIME_ATTACK", "SCORE_ATTACK"
    val score: Int,
    val timestamp: Long
)
