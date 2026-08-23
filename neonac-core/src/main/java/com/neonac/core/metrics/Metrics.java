package com.neonac.core.metrics;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
public final class Metrics {

    private volatile boolean enabled = true;

    private final LongAdder checksExecuted = new LongAdder();
    private final LongAdder detections = new LongAdder();
    private final LongAdder violations = new LongAdder();
    private final LongAdder falsePositiveResets = new LongAdder();
    private final AtomicLong totalProcessingNanos = new AtomicLong();
    private final LongAdder packetsProcessed = new LongAdder();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void recordCheck() {
        if (enabled) checksExecuted.increment();
    }

    public void recordDetection() {
        if (enabled) detections.increment();
    }

    public void recordViolation() {
        if (enabled) violations.increment();
    }

    public void recordReset() {
        if (enabled) falsePositiveResets.increment();
    }

    public void recordProcessing(long nanos) {
        if (enabled) totalProcessingNanos.addAndGet(nanos);
    }

    public void recordPacket() {
        if (enabled) packetsProcessed.increment();
    }

    public Snapshot snapshot() {
        long exec = checksExecuted.sum();
        long proc = totalProcessingNanos.get();
        return new Snapshot(exec, detections.sum(), violations.sum(),
                falsePositiveResets.sum(), packetsProcessed.sum(),
                exec > 0 ? (proc / (double) exec) : 0.0);
    }

    public static final class Snapshot {
        public final long checksExecuted;
        public final long detections;
        public final long violations;
        public final long falsePositiveResets;
        public final long packetsProcessed;
        public final double avgProcessingNanos;

        Snapshot(long checksExecuted, long detections, long violations,
                 long falsePositiveResets, long packetsProcessed, double avgProcessingNanos) {
            this.checksExecuted = checksExecuted;
            this.detections = detections;
            this.violations = violations;
            this.falsePositiveResets = falsePositiveResets;
            this.packetsProcessed = packetsProcessed;
            this.avgProcessingNanos = avgProcessingNanos;
        }
    }
}
