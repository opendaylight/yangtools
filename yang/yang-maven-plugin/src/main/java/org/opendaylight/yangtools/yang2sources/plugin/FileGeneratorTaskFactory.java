/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFilePath;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Bridge to a {@link FileGenerator} instance.
 *
 * @author Robert Varga
 */
final class FileGeneratorTaskFactory extends GeneratorTaskFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FileGeneratorTaskFactory.class);
    private static final CharMatcher SEP_MATCHER = CharMatcher.is(GeneratedFilePath.SEPARATOR);
    private static final Map<GeneratedFileType, String> TYPE_PATHS = ImmutableMap.of(
        GeneratedFileType.RESOURCE, "generated-resources", GeneratedFileType.TEST_RESOURCE, "generated-test-resources",
        GeneratedFileType.SOURCE, "generated-sources", GeneratedFileType.TEST_SOURCE, "generated-test-sources");

    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    private File buildDir;
    private File sourceDir;
    private ContextHolder context;

    private FileGeneratorTaskFactory(final FileGenerator gen, final FileGeneratorArg arg) {
        super(gen.importResolutionMode());
        this.arg = arg;
        this.gen = gen;
    }

    static FileGeneratorTaskFactory of(final FileGeneratorFactory factory, final FileGeneratorArg arg)
            throws FileGeneratorException {
        return new FileGeneratorTaskFactory(factory.newFileGenerator(arg.getConfiguration()), arg);
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder context) {
        this.context = requireNonNull(context);
        final Build build = project.getModel().getBuild();
        buildDir = new File(build.getDirectory());
        sourceDir = new File(project.getBasedir(), build.getSourceDirectory()).getParentFile();
    }

    @Override
    Collection<File> execute(final BuildContext buildContext) throws FileGeneratorException, IOException {
        final ContextHolder local = verifyNotNull(context);

        // Step one: determine what files are going to be generated
        final Stopwatch sw = Stopwatch.createStarted();
        final Table<GeneratedFileType, GeneratedFilePath, GeneratedFile> generatedFiles = gen.generateFiles(
            local.getContext(), local.getYangModules(), local);
        LOG.info("{}: Defined {} files in {}", arg.getIdentifier(), generatedFiles.size(), sw);

        final File persistentSourcesDir = null; // src/main
        final File outputBaseDir = null; // target

        // Step two: create generation tasks for each target file and group them by parent directory
        sw.reset().start();
        final ListMultimap<File, WriteTask> dirs = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Cell<GeneratedFileType, GeneratedFilePath, GeneratedFile> cell : generatedFiles.cellSet()) {
            final GeneratedFile file = cell.getValue();
            final String relativePath = cell.getColumnKey().getPath();
            final File target;
            switch (file.getLifecycle()) {
                case PERSISTENT:
                    target = new File(filePath(sourceDir, cell.getRowKey()), relativePath);
                    if (target.exists()) {
                        LOG.debug("Skipping existing persistent {}", target);
                        continue;
                    }
                    break;
                case TRANSIENT:
                    target = new File(filePath(buildDir, cell.getRowKey()), relativePath);
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

    private File filePath(final File parentDir, final GeneratedFileType fileType) throws FileGeneratorException {
        final String path = TYPE_PATHS.get(fileType);
        if (path == null) {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }

        return new File(buildDir, path + File.separatorChar + arg.getIdentifier());
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
