package de.pixelregion.region;

import java.util.ArrayList;
import java.util.List;

public final class RegionGeometry {

    private final List<RegionPoint> points;
    private final RegionBounds bounds;

    public RegionGeometry(final List<RegionPoint> source) {
        if (source == null || source.size() < 3) {
            throw new IllegalArgumentException("A region polygon requires at least three points.");
        }

        final List<RegionPoint> copy = List.copyOf(source);
        for (int i = 0; i < copy.size(); i++) {
            final RegionPoint current = copy.get(i);
            final RegionPoint next = copy.get((i + 1) % copy.size());
            if (current.equals(next)) {
                throw new IllegalArgumentException("Adjacent polygon points may not be identical.");
            }
        }

        if (Math.abs(signedArea(copy)) < 1.0E-8) {
            throw new IllegalArgumentException("Region polygon area must be non-zero.");
        }

        if (hasSelfIntersection(copy)) {
            throw new IllegalArgumentException("Region polygon may not self-intersect.");
        }

        this.points = copy;
        this.bounds = calculateBounds(copy);
    }

    public List<RegionPoint> points() {
        return points;
    }

    public RegionBounds bounds() {
        return bounds;
    }

    public boolean contains(final double x, final double z) {
        if (!bounds.contains(x, z)) {
            return false;
        }

        boolean inside = false;
        for (int i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            final RegionPoint a = points.get(i);
            final RegionPoint b = points.get(j);

            if (onSegment(a.x(), a.z(), b.x(), b.z(), x, z)) {
                return true;
            }

            final boolean intersects = ((a.z() > z) != (b.z() > z))
                    && (x < (b.x() - a.x()) * (z - a.z()) / (b.z() - a.z()) + a.x());
            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static RegionBounds calculateBounds(final List<RegionPoint> points) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (final RegionPoint point : points) {
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
            minZ = Math.min(minZ, point.z());
            maxZ = Math.max(maxZ, point.z());
        }
        return new RegionBounds(minX, maxX, minZ, maxZ);
    }

    private static double signedArea(final List<RegionPoint> points) {
        double area = 0.0;
        for (int i = 0; i < points.size(); i++) {
            final RegionPoint a = points.get(i);
            final RegionPoint b = points.get((i + 1) % points.size());
            area += a.x() * b.z() - b.x() * a.z();
        }
        return area / 2.0;
    }

    private static boolean onSegment(
            final double ax,
            final double az,
            final double bx,
            final double bz,
            final double px,
            final double pz
    ) {
        final double cross = (px - ax) * (bz - az) - (pz - az) * (bx - ax);
        if (Math.abs(cross) > 1.0E-8) {
            return false;
        }
        return px >= Math.min(ax, bx) - 1.0E-8
                && px <= Math.max(ax, bx) + 1.0E-8
                && pz >= Math.min(az, bz) - 1.0E-8
                && pz <= Math.max(az, bz) + 1.0E-8;
    }

    private static boolean hasSelfIntersection(final List<RegionPoint> points) {
        final int size = points.size();
        for (int i = 0; i < size; i++) {
            final RegionPoint a1 = points.get(i);
            final RegionPoint a2 = points.get((i + 1) % size);

            for (int j = i + 1; j < size; j++) {
                if (i == j || (i + 1) % size == j || i == (j + 1) % size) {
                    continue;
                }

                final RegionPoint b1 = points.get(j);
                final RegionPoint b2 = points.get((j + 1) % size);

                if (segmentsIntersect(a1, a2, b1, b2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean segmentsIntersect(
            final RegionPoint a,
            final RegionPoint b,
            final RegionPoint c,
            final RegionPoint d
    ) {
        final double abC = orientation(a, b, c);
        final double abD = orientation(a, b, d);
        final double cdA = orientation(c, d, a);
        final double cdB = orientation(c, d, b);

        if (((abC > 0 && abD < 0) || (abC < 0 && abD > 0))
                && ((cdA > 0 && cdB < 0) || (cdA < 0 && cdB > 0))) {
            return true;
        }

        return (Math.abs(abC) < 1.0E-8 && onSegment(a.x(), a.z(), b.x(), b.z(), c.x(), c.z()))
                || (Math.abs(abD) < 1.0E-8 && onSegment(a.x(), a.z(), b.x(), b.z(), d.x(), d.z()))
                || (Math.abs(cdA) < 1.0E-8 && onSegment(c.x(), c.z(), d.x(), d.z(), a.x(), a.z()))
                || (Math.abs(cdB) < 1.0E-8 && onSegment(c.x(), c.z(), d.x(), d.z(), b.x(), b.z()));
    }

    private static double orientation(final RegionPoint a, final RegionPoint b, final RegionPoint c) {
        return (b.x() - a.x()) * (c.z() - a.z()) - (b.z() - a.z()) * (c.x() - a.x());
    }
}
