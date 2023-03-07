/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.MultimapBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
final class GeneratorTask implements Identifiable<String> {
    private static final Logger LOG = LoggerFactory.getLogger(GeneratorTask.class);

    private final YangParserConfiguration parserConfig;
    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    GeneratorTask(final FileGeneratorFactory factory, final FileGeneratorArg arg) throws FileGeneratorException {
        this.arg = requireNonNull(arg);
        gen = factory.newFileGenerator(arg.getConfiguration());
        parserConfig = switch (gen.importResolutionMode()) {
            case REVISION_EXACT_OR_LATEST -> YangParserConfiguration.DEFAULT;
        };
    }

    @Override
    public String getIdentifier() {
        return arg.getIdentifier();
    }

    YangParserConfiguration parserConfig() {
        return parserConfig;
    }

    FileGeneratorArg arg() {
        return arg;
    }

    String generatorName() {
        return gen.getClass().getName();
    }

    /**
     * Create a new {@link GeneratorTask} which will work in scope of specified {@link MavenProject} with the effective
     * model held in specified {@link ContextHolder}.
     *
     * @param project current Maven Project
     * @param context model generation context
     * @return {@link FileState} for every generated file
     * @throws FileGeneratorException if the underlying generator fails
     * @throws IOException when a generated file cannot be written
     */
    List<FileState> execute(final MavenProject project, final ContextHolder context)
            throws FileGeneratorException, IOException {
        final var access = new ProjectFileAccess(project, getIdentifier());

        // Step one: determine what files are going to be generated
        final var sw = Stopwatch.createStarted();
        final var generatedFiles = gen.generateFiles(context.getContext(), context.getYangModules(), context);
        LOG.info("{}: Defined {} files in {}", getIdentifier(), generatedFiles.size(), sw);

        // Step two: create generation tasks for each target file and group them by parent directory
        sw.reset().start();
        final var dirs = MultimapBuilder.hashKeys().arrayListValues().<File, WriteTask>build();
        for (var cell : generatedFiles.cellSet()) {
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("generator", generatorName()).add("argument", arg).toString();
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
