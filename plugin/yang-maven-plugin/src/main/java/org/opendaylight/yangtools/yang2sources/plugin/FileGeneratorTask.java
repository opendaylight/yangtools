/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

final class FileGeneratorTask extends GeneratorTask<FileGeneratorTaskFactory> {
    private static final Logger LOG = LoggerFactory.getLogger(FileGeneratorTask.class);
    private static final Map<GeneratedFileType, String> TYPE_PATHS = ImmutableMap.of(
        GeneratedFileType.RESOURCE, "generated-resources",
        GeneratedFileType.TEST_RESOURCE, "generated-test-resources",
        GeneratedFileType.SOURCE, "generated-sources",
        GeneratedFileType.TEST_SOURCE, "generated-test-sources");

    private final Map<GeneratedFileType, File> fileDirs = new HashMap<>();
    private final MavenProject project;
    private final File buildDir;
    private final File sourceDir;
    private final String suffix;

    FileGeneratorTask(final @NonNull FileGeneratorTaskFactory factory, final @NonNull ContextHolder context,
            final MavenProject project) {
        super(factory, context);

        final Build build = project.getModel().getBuild();
        final String buildDirectory = build.getDirectory();
        this.buildDir = new File(buildDirectory);
        this.sourceDir = new File(project.getBasedir(), build.getSourceDirectory()).getParentFile();
        this.suffix = factory.getIdentifier();
        this.project = project;
    }

    @Override
    Collection<File> execute(final FileGeneratorTaskFactory factory, final ContextHolder modelContext,
            final BuildContext buildContext) throws FileGeneratorException, IOException {
        // Step one: determine what files are going to be generated
        final Stopwatch sw = Stopwatch.createStarted();
        final FileGenerator gen = factory.generator();
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generatedFiles = gen.generateFiles(
            modelContext.getContext(), modelContext.getYangModules(), modelContext);
        LOG.info("{}: Defined {} files in {}", suffix, generatedFiles.size(), sw);

        // Step two: create generation tasks for each target file and group them by parent directory
        sw.reset().start();
        final ListMultimap<File, WriteTask> dirs = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Cell<GeneratedFileType, GeneratedFilePath, GeneratedFile> cell : generatedFiles.cellSet()) {
            final GeneratedFile file = cell.getValue();
            final String relativePath = cell.getColumnKey().getPath();
            final File target;
            switch (file.getLifecycle()) {
                case PERSISTENT:
                    target = new File(persistentPath(cell.getRowKey()), relativePath);
                    if (target.exists()) {
                        LOG.debug("Skipping existing persistent {}", target);
                        continue;
                    }
                    break;
                case TRANSIENT:
                    target = new File(transientPath(cell.getRowKey()), relativePath);
                    break;
                default:
                    throw new IllegalStateException("Unsupported file type in " + file);
            }

            dirs.put(target.getParentFile(), new WriteTask(buildContext, target, cell.getValue()));
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
        final List<File> result = dirs.values().parallelStream()
                .map(WriteTask::generateFile)
                .collect(Collectors.toList());
        LOG.debug("Generated {} files in {}", result.size(), sw);

        return result;
    }

    private File persistentPath(final GeneratedFileType fileType) throws FileGeneratorException {
        return filePath(sourceDir, fileType);
    }

    private File transientPath(final GeneratedFileType fileType) throws FileGeneratorException {
        final File existing = fileDirs.get(fileType);
        if (existing != null) {
            return existing;
        }

        final File newDir = newFilePath(buildDir, fileType);
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            project.addCompileSourceRoot(newDir.toString());
        }
        return newDir;
    }

    private File filePath(final File parentDir, final GeneratedFileType fileType) throws FileGeneratorException {
        final File existing = fileDirs.get(fileType);
        return existing != null ? existing : newFilePath(parentDir, fileType);
    }

    private File newFilePath(final File parentDir, final GeneratedFileType fileType) throws FileGeneratorException {
        final String path = TYPE_PATHS.get(fileType);
        if (path == null) {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }
        final File ret = new File(parentDir, path + File.separatorChar + suffix);
        verify(fileDirs.put(fileType, ret) == null);
        return ret;
    }

    private static final class WriteTask {
        private final BuildContext buildContext;
        private final GeneratedFile file;
        private final File target;

        WriteTask(final BuildContext buildContext, final File target, final GeneratedFile file) {
            this.buildContext = requireNonNull(buildContext);
            this.target = requireNonNull(target);
            this.file = requireNonNull(file);
        }

        File generateFile() {
            try (OutputStream stream = buildContext.newFileOutputStream(target)) {
                file.writeBody(stream);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to generate file " + target, e);
            }
            return target;
        }
    }
}
