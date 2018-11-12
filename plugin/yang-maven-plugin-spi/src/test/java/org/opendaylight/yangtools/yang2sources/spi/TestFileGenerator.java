/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.plugin.generator.api.ModuleResourceResolver;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

@NonNullByDefault
final class TestFileGenerator implements FileGenerator {
    private final String prefix;

    TestFileGenerator(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generateFiles(final EffectiveModelContext context,
            final Set<Module> localModules, final ModuleResourceResolver moduleResourcePathResolver) {
        if (prefix == null) {
            return ImmutableTable.of();
        }

        return ImmutableTable.<GeneratedFileType, GeneratedFilePath, GeneratedFile>builder()
            .put(GeneratedFileType.SOURCE, GeneratedFilePath.ofFilePath(prefix + "Source.test"),
                GeneratedFile.of(GeneratedFileLifecycle.PERSISTENT, "source"))
            .put(GeneratedFileType.RESOURCE, GeneratedFilePath.ofFilePath(prefix + "-resource"),
                GeneratedFile.of(GeneratedFileLifecycle.PERSISTENT, "test-resource"))
            .put(GeneratedFileType.SOURCE, GeneratedFilePath.ofFilePath(prefix + "GenSource.test"),
                GeneratedFile.of(GeneratedFileLifecycle.TRANSIENT, "source"))
            .put(GeneratedFileType.RESOURCE, GeneratedFilePath.ofFilePath(prefix + "-gen-resource"),
                GeneratedFile.of(GeneratedFileLifecycle.TRANSIENT, "resource"))
            .build();
    }
}
