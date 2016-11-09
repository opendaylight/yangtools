/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;

/**
 * SupportedVersionsBundle contains information about supported versions.
 * StatementSupportBundle objects are initialized based on information provided
 * by objects of this class.
 */
@Beta
public class SupportedVersionsBundle {
    private final Map<String, SemVer> supportedVersionMap;
    private final Set<SemVer> supportedVersions;

    public SupportedVersionsBundle(final Map<String, SemVer> supportedVersionMap) {
        Preconditions.checkNotNull(supportedVersionMap);
        Preconditions.checkArgument(!supportedVersionMap.containsValue(null));
        this.supportedVersionMap = ImmutableMap.copyOf(supportedVersionMap);
        this.supportedVersions = new ImmutableSet.Builder<SemVer>().addAll(supportedVersionMap.values()).build();
    }

    /**
     * Returns SemVer object corresponding to given string version or returns
     * null if either a string version is not in appropriate form or it is not
     * supported.
     *
     * @param version
     *            string representation of a version
     * @return SemVer corresponding to given string version or null, if either a
     *         string version is not in appropriate form or it is not supported
     */
    @Nullable
    public SemVer getSupportedVersion(final String version) {
        return supportedVersionMap.get(version);
    }

    /**
     * Returns set of supported versions.
     *
     * @return set of supported versions
     */
    @Nonnull
    public Set<SemVer> getAll() {
        return supportedVersions;
    }

    /**
     * Checks whether given version is supported.
     *
     * @param version
     *            version to be checked, whether it is supported or not
     * @return true if a version is supported, otherwise false
     */
    public boolean contains(final SemVer version) {
        return supportedVersions.contains(version);
    }
}
