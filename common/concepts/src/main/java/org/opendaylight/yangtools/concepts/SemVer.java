/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
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
        Preconditions.checkArgument(major >= 0);
        this.major = major;
        Preconditions.checkArgument(minor >= 0);
        this.minor = minor;
        Preconditions.checkArgument(patch >= 0);
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

    public static SemVer valueOf(@Nonnull final String s) {
        final int minorIdx = s.indexOf('.');
        if (minorIdx == -1) {
            return create(Integer.parseInt(s));
        }

        final String minorStr;
        final int patchIdx = s.indexOf('.', minorIdx + 1);
        if (patchIdx == -1) {
            minorStr = s.substring(minorIdx + 1);
        } else {
            minorStr = s.substring(minorIdx + 1, patchIdx);
        }

        return create(Integer.parseInt(s.substring(0, minorIdx), 10), Integer.parseInt(minorStr, 10),
            Integer.parseInt(s.substring(patchIdx + 1), 10));
    }

    /**
     * @return the major
     */
    public int getMajor() {
        return major;
    }

    /**
     * @return the minor
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @return the patch
     */
    public int getPatch() {
        return patch;
    }

    @Override
    public int compareTo(final SemVer o) {
        int i = Integer.compare(major, o.major);
        if (i == 0) {
            i = Integer.compare(minor, o.minor);
            if (i == 0) {
                return Integer.compare(patch, o.patch);
            }
        }

        return i;
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
