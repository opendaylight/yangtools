/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A single version according to <a href="http://semver.org/">Semantic Versioning</a>.
 */
@Beta
public final class SemVer implements Comparable<SemVer>, Serializable {
    private static final long serialVersionUID = 1L;
    private final int major;
    private final int minor;
    private final int patch;

    private SemVer(final int major, final int minor, final int patch) {
        checkArgument(major >= 0);
        this.major = major;
        checkArgument(minor >= 0);
        this.minor = minor;
        checkArgument(patch >= 0);
        this.patch = patch;
    }

    public static SemVer create(final int major) {
        return create(major, 0);
    }

    public static SemVer create(final int major, final int minor) {
        return create(major, minor, 0);
    }

    public static SemVer create(final int major, final int minor, final int patch) {
        return new SemVer(major, minor, patch);
    }

    public static SemVer valueOf(@Nonnull final String str) {
        final int minorIdx = str.indexOf('.');
        if (minorIdx == -1) {
            return create(Integer.parseInt(str));
        }

        final String minorStr;
        final int patchIdx = str.indexOf('.', minorIdx + 1);
        if (patchIdx == -1) {
            minorStr = str.substring(minorIdx + 1);
            return create(Integer.parseInt(str.substring(0, minorIdx), 10), Integer.parseInt(minorStr, 10));
        }

        minorStr = str.substring(minorIdx + 1, patchIdx);
        return create(Integer.parseInt(str.substring(0, minorIdx), 10), Integer.parseInt(minorStr, 10),
            Integer.parseInt(str.substring(patchIdx + 1), 10));
    }

    /**
     * Return the major version number.
     *
     * @return major version number
     */
    public int getMajor() {
        return major;
    }

    /**
     * Return the minor version number.
     *
     * @return minor version number
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Return the patch version number.
     *
     * @return patch version number
     */
    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(@Nonnull final SemVer other) {
        int cmp = Integer.compare(major, other.major);
        if (cmp == 0) {
            cmp = Integer.compare(minor, other.minor);
            if (cmp == 0) {
                return Integer.compare(patch, other.patch);
            }
        }

        return cmp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SemVer)) {
            return false;
        }

        final SemVer o = (SemVer) obj;
        return major == o.major && minor == o.minor && patch == o.patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
