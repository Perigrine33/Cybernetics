package com.perigrine3.createcybernetics.client.navigation;

/** Shared work allowance for a displayed frame, including all catch-up ticks. */
public final class NavigationFrameBudget {
    private final int maxSamples;
    private final long maxNanos;
    private boolean workClaimed;
    private int samples;
    private long startedNanos;

    public NavigationFrameBudget(int maxSamples, long maxNanos) {
        this.maxSamples = maxSamples;
        this.maxNanos = maxNanos;
    }

    public void beginFrame() {
        workClaimed = false;
        samples = 0;
    }

    public boolean claimWork() {
        if (workClaimed) return false;
        workClaimed = true;
        return true;
    }

    public boolean claimSample(long nowNanos) {
        if (samples >= maxSamples || (samples > 0 && nowNanos - startedNanos >= maxNanos)) return false;
        if (samples == 0) startedNanos = nowNanos;
        samples++;
        return true;
    }
}
