/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.SemVer;

public class SupportedVersionsBundle {
    private final Map<String, SemVer> supportedVersionMap;
    private final Set<SemVer> supportedVersions;

    public SupportedVersionsBundle(final Map<String, SemVer> supportedVersionMap) {
        Preconditions.checkNotNull(supportedVersionMap);
        Preconditions.checkArgument(!supportedVersionMap.containsValue(null));
        this.supportedVersionMap = ImmutableMap.copyOf(supportedVersionMap);
        this.supportedVersions = new ImmutableSet.Builder<SemVer>().addAll(supportedVersionMap.values()).build();
    }

    public SemVer getSupportedVersion(final String version) {
        return supportedVersionMap.get(version);
    }

    public Set<SemVer> getAll() {
        return supportedVersions;
    }

    public boolean contains(final SemVer version) {
        return supportedVersions.contains(version);
    }
}
