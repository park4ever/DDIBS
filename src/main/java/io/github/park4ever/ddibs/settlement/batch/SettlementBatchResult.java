package io.github.park4ever.ddibs.settlement.batch;

public record SettlementBatchResult(
        int candidateCount,
        int createdCount,
        int raceSkippedCount
) {
}
