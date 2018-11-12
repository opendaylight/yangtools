/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Build information used to compare inputs that went it a particular build with what is available in the next build.
 */
final class BuildInfo implements Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    final ImmutableMap<String, InputFile> inputFiles;
    final ImmutableSet<File> outputFiles;

    private BuildInfo(final Map<String, InputFile> inputFiles, final Set<File> outputFiles) {
        this.inputFiles = ImmutableMap.copyOf(inputFiles);
        this.outputFiles = ImmutableSet.copyOf(outputFiles);
    }

    static BuildInfo create(final Collection<File> inputFiles, final Set<File> outputFiles) {
        return new BuildInfo(Maps.uniqueIndex(Collections2.transform(inputFiles, input -> {
            try {
                return InputFile.of(input);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to process file " + input, e);
            }
        }), input -> input.fileName), outputFiles);
    }
}
