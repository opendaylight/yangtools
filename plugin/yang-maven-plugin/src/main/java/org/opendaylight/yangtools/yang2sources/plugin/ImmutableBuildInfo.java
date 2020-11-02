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
import com.google.common.collect.ImmutableMultimap;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
final class ImmutableBuildInfo extends BuildInfo {
    private final BuildConfiguration buildConfiguration;
    private final ImmutableList<HashedFile> inputFiles;
    private final ImmutableMultimap<String, HashedFile> outputFiles;

    ImmutableBuildInfo(final BuildConfiguration buildConfiguration, final ImmutableList<HashedFile> inputFiles,
        final ImmutableMultimap<String, HashedFile> outputFiles) {
        this.buildConfiguration = requireNonNull(buildConfiguration);
        this.inputFiles = requireNonNull(inputFiles);
        this.outputFiles = requireNonNull(outputFiles);
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
    ImmutableMultimap<String, HashedFile> outputFiles() throws IOException {
        return outputFiles;
    }

    @Override
    ImmutableBuildInfo toImmutable() {
        return this;
    }
}
