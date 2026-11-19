package com.perigrine3.createcybernetics.client.navigation;

import java.util.ArrayList;
import java.util.Comparator;

/** Nearby terrain first: a moving player must not repeatedly scan only a far edge. */
public final class NavigationExplorationScan {
    private final int radius;
    private final int diameter;
    private final int[] offsets;

    public NavigationExplorationScan(int radius) {
        if (radius < 0) throw new IllegalArgumentException("radius must be nonnegative");
        this.radius = radius;
        diameter = Math.addExact(Math.multiplyExact(radius, 2), 1);
        int area = Math.multiplyExact(diameter, diameter);
        var positions = new ArrayList<Integer>();
        for (int index = 0; index < area; index++) {
            if (distanceSquared(index) <= (long) radius * radius) positions.add(index);
        }
        positions.sort(Comparator.comparingLong(this::distanceSquared));
        offsets = positions.stream().mapToInt(Integer::intValue).toArray();
    }

    private long distanceSquared(int index) {
        long x = index % diameter - radius;
        long z = index / diameter - radius;
        return x * x + z * z;
    }

    public int size() { return offsets.length; }
    public int x(int index) { return offsets[index] % diameter - radius; }
    public int z(int index) { return offsets[index] / diameter - radius; }
}
