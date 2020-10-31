/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class ImmutableBuildInfo extends BuildInfo {
    private final BuildConfiguration buildConfiguration;
    private final ImmutableList<HashedFile> inputFiles;

    ImmutableBuildInfo(final BuildConfiguration buildConfiguration, final ImmutableList<HashedFile> inputFiles) {
        this.buildConfiguration = requireNonNull(buildConfiguration);
        this.inputFiles = requireNonNull(inputFiles);
    }

    @Override
    BuildConfiguration buildConfiguration() {
        return buildConfiguration;
    }

    @Override
    ImmutableList<HashedFile> inputFiles() {
        return inputFiles;
    }

    @Override
    ImmutableBuildInfo toImmutable() {
        return this;
    }
}
