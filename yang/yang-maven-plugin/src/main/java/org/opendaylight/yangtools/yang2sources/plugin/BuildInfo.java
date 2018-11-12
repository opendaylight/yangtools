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
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileKind;
import org.opendaylight.yangtools.yang.plugin.generator.api.GeneratedFileLifecycle;

/**
 * Build information used to compare inputs that went it a particular build with what is available in the next build.
 */
final class BuildInfo implements Immutable, Serializable {
    private static final long serialVersionUID = 1L;

    final ImmutableTable<GeneratedFileKind, String, GeneratedFileLifecycle> outputFiles;
    final ImmutableMap<String, InputFile> inputFiles;

    private BuildInfo(final Map<String, InputFile> inputFiles,
            final Table<GeneratedFileKind, String, GeneratedFileLifecycle> outputFiles) {
        this.inputFiles = ImmutableMap.copyOf(inputFiles);
        this.outputFiles = ImmutableTable.copyOf(outputFiles);
    }

    static BuildInfo create(final Collection<File> inputFiles,
            final Table<GeneratedFileKind, String, GeneratedFile> outputFiles) {
        return new BuildInfo(Maps.uniqueIndex(Collections2.transform(inputFiles, input -> {
            try {
                return InputFile.of(input);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to process file " + input, e);
            }
        }), input -> input.fileName), Tables.transformValues(outputFiles, GeneratedFile::getLifecycle));
    }
}
