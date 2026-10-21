package flowerdelivery.facade;

public enum ProcessStatus {
    PENDING_B,      // A сақталды, B күтілуде
    PENDING_C,      // A және B сақталды, C күтілуде
    COMPLETED,
    COMPENSATING,
    COMPENSATED
}