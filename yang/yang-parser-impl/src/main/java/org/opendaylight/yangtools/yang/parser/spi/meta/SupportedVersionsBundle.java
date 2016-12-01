/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.YangVersion;

/**
 * SupportedVersionsBundle contains information about supported versions.
 * StatementSupportBundle objects are initialized based on information provided
 * by objects of this class.
 */
@Beta
public class SupportedVersionsBundle {
    private final Set<YangVersion> supportedVersions;

    public SupportedVersionsBundle(final Set<YangVersion> supportedVersions) {
        this.supportedVersions = ImmutableSet.copyOf(supportedVersions);
    }

    /**
     * Returns set of supported versions.
     *
     * @return set of supported versions
     */
    @Nonnull
    public Set<YangVersion> getAll() {
        return supportedVersions;
    }

    /**
     * Checks whether given version is supported.
     *
     * @param version
     *            version to be checked, whether it is supported or not
     * @return true if a version is supported, otherwise false
     */
    public boolean contains(final YangVersion version) {
        return supportedVersions.contains(version);
    }
}
