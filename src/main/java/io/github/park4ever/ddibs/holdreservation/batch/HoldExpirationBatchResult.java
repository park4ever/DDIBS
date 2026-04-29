package io.github.park4ever.ddibs.holdreservation.batch;

public record HoldExpirationBatchResult(
        int candidateCount,
        int expiredCount,
        int orderStateSkippedCount,
        int holdStateSkippedCount
) {
}
