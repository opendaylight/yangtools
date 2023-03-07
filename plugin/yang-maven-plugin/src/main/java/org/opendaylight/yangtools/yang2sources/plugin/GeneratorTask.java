/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GeneratorTask extends ParserConfigAware {
    private static final Logger LOG = LoggerFactory.getLogger(GeneratorTask.class);

    private final @NonNull GeneratorTaskFactory factory;
    private final @NonNull ContextHolder contextHolder;
    private final @NonNull ProjectFileAccess access;

    GeneratorTask(final @NonNull GeneratorTaskFactory factory, final @NonNull ContextHolder contextHolder,
            final ProjectFileAccess access) {
        this.factory = requireNonNull(factory);
        this.contextHolder = requireNonNull(contextHolder);
        this.access = requireNonNull(access);
    }

    @Override
    YangParserConfiguration parserConfig() {
        return factory.parserConfig();
    }

    List<FileState> execute() throws FileGeneratorException, IOException {
        // Step one: determine what files are going to be generated
        final Stopwatch sw = Stopwatch.createStarted();
        final FileGenerator gen = factory.generator();
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generatedFiles = gen.generateFiles(
            contextHolder.getContext(), contextHolder.getYangModules(), contextHolder);
        LOG.info("{}: Defined {} files in {}", factory.getIdentifier(), generatedFiles.size(), sw);

        // Step two: create generation tasks for each target file and group them by parent directory
        sw.reset().start();
        final ListMultimap<File, WriteTask> dirs = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Cell<GeneratedFileType, GeneratedFilePath, GeneratedFile> cell : generatedFiles.cellSet()) {
            final GeneratedFile file = cell.getValue();
            final String relativePath = cell.getColumnKey().getPath();
            final File target;
            switch (file.getLifecycle()) {
                case PERSISTENT:
                    target = new File(access.persistentPath(cell.getRowKey()), relativePath);
                    if (target.exists()) {
                        LOG.debug("Skipping existing persistent {}", target);
                        continue;
                    }
                    break;
                case TRANSIENT:
                    target = new File(access.transientPath(cell.getRowKey()), relativePath);
                    break;
                default:
                    throw new IllegalStateException("Unsupported file type in " + file);
            }

            dirs.put(target.getParentFile(), new WriteTask(target, cell.getValue()));
        }
        LOG.info("Sorted {} files into {} directories in {}", dirs.size(), dirs.keySet().size(), sw);

        // Step three: submit parent directory creation tasks (via parallelStream()) and wait for them to complete
        sw.reset().start();
        dirs.keySet().parallelStream().forEach(path -> {
            try {
                Files.createDirectories(path.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create " + path, e);
            }
        });
        LOG.debug("Parent directories created in {}", sw);

        // Step four: submit all code generation tasks (via parallelStream()) and wait for them to complete
        sw.reset().start();
        final var result = dirs.values().parallelStream()
                .map(WriteTask::generateFile)
                .collect(Collectors.toList());
        LOG.debug("Generated {} files in {}", result.size(), sw);

        access.updateMavenProject();
        return result;
    }

    private static final class WriteTask {
        private final GeneratedFile file;
        private final File target;

        WriteTask(final File target, final GeneratedFile file) {
            this.target = requireNonNull(target);
            this.file = requireNonNull(file);
        }

        FileState generateFile() {
            final FileState ret;
            try {
                ret = FileState.ofWrittenFile(target, file::writeBody);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to generate file " + target, e);
            }
            return ret;
        }
    }
}
