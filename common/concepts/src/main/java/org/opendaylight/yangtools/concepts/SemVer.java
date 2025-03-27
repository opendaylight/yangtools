/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A single version according to <a href="http://semver.org/">Semantic Versioning</a>.
 *
 * @param major Major version number
 * @param minor Minor version number
 * @param patch Patch version number
 */
public record SemVer(int major, int minor, int patch) implements Comparable<SemVer>, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public SemVer {
        if (major < 0) {
            throw new IllegalArgumentException("Major version has to be non-negative");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor version has to be non-negative");
        }
        if (patch < 0) {
            throw new IllegalArgumentException("Patch version has to be non-negative");
        }
    }

    public SemVer(final int major) {
        this(major, 0);
    }

    public SemVer(final int major, final int minor) {
        this(major, minor, 0);
    }

    public static @NonNull SemVer valueOf(final @NonNull String str) {
        final int minorIdx = str.indexOf('.');
        if (minorIdx == -1) {
            return new SemVer(Integer.parseInt(str));
        }

        final String minorStr;
        final int patchIdx = str.indexOf('.', minorIdx + 1);
        if (patchIdx == -1) {
            minorStr = str.substring(minorIdx + 1);
            return new SemVer(Integer.parseInt(str.substring(0, minorIdx), 10), Integer.parseInt(minorStr, 10));
        }

        minorStr = str.substring(minorIdx + 1, patchIdx);
        return new SemVer(Integer.parseInt(str.substring(0, minorIdx), 10), Integer.parseInt(minorStr, 10),
            Integer.parseInt(str.substring(patchIdx + 1), 10));
    }

    @Override
    public int compareTo(final SemVer other) {
        int cmp = Integer.compare(major, other.major);
        if (cmp == 0) {
            cmp = Integer.compare(minor, other.minor);
            if (cmp == 0) {
                cmp = Integer.compare(patch, other.patch);
            }
        }
        return cmp;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
