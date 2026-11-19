package com.perigrine3.createcybernetics.client.navigation;

import java.util.BitSet;
import java.util.function.IntBinaryOperator;

/** A moving window with a bounded sampling cursor. Zero is an unavailable sample. */
public final class NavigationTerrainGrid {
    private final int size;
    private int[] colors;
    private int[] scratch;
    private final BitSet missing;
    private int centerX;
    private int centerZ;
    private boolean initialized;
    private int missingCursor;
    private int refreshCursor;
    private boolean changed;

    public NavigationTerrainGrid(int size) {
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
        this.size = size;
        colors = new int[size * size];
        scratch = new int[colors.length];
        missing = new BitSet(colors.length);
    }

    public void moveTo(int x, int z) {
        if (initialized && centerX == x && centerZ == z) return;
        long dx = initialized ? (long) x - centerX : size;
        long dz = initialized ? (long) z - centerZ : size;
        missing.clear();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                long oldX = col + dx;
                long oldZ = row + dz;
                int index = row * size + col;
                scratch[index] = oldX >= 0 && oldX < size && oldZ >= 0 && oldZ < size
                        ? colors[(int) oldZ * size + (int) oldX] : 0;
                if (scratch[index] == 0) missing.set(index);
            }
        }
        int[] previous = colors;
        colors = scratch;
        scratch = previous;
        centerX = x;
        centerZ = z;
        initialized = true;
        changed = true;
    }

    public void update(IntBinaryOperator sampler, int budget) {
        if (!initialized || budget <= 0) return;
        // Retry unavailable columns fairly. They cannot force a complete map rebuild.
        int attempts = Math.min(budget, missing.cardinality());
        for (int count = 0; count < attempts; count++) {
            int index = missing.nextSetBit(missingCursor);
            if (index < 0) index = missing.nextSetBit(0);
            if (index < 0) break;
            sample(sampler, index);
            missingCursor = (index + 1) % colors.length;
            budget--;
        }
        while (budget-- > 0) {
            sample(sampler, refreshCursor);
            refreshCursor = (refreshCursor + 1) % colors.length;
        }
    }

    private void sample(IntBinaryOperator sampler, int index) {
        int color = sampler.applyAsInt(centerX + index % size - size / 2, centerZ + index / size - size / 2);
        if (color == 0) return;
        missing.clear(index);
        if (colors[index] != color) { colors[index] = color; changed = true; }
    }

    public int color(int x, int z) { return colors[z * size + x]; }

    public boolean consumeChanged() {
        boolean result = changed;
        changed = false;
        return result;
    }
}
