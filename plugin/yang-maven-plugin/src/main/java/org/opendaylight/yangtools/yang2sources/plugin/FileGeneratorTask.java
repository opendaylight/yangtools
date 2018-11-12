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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private final File buildDir;
    private final File sourceDir;
    private final String suffix;

    FileGeneratorTask(final @NonNull FileGeneratorTaskFactory factory, final @NonNull ContextHolder context,
            final File buildDir, final File sourceDir) {
        super(factory, context);
        this.buildDir = requireNonNull(buildDir);
        this.sourceDir = requireNonNull(sourceDir);
        this.suffix = factory.getIdentifier();
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

        return new File(buildDir, path + File.separatorChar + suffix);
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
            final HashCode hashCode;
            try (OutputStream stream = buildContext.newFileOutputStream(target)) {
                try (HashingOutputStream hashing = new HashingOutputStream(stream)) {
                    file.writeBody(hashing);
                    hashCode = hashing.hash();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to generate file " + target, e);
            }
            return target;
        }
    }

    private static final class HashingOutputStream extends OutputStream {
        private final Hasher hasher = InputFile.HASH_FUNCTION.newHasher();
        private final OutputStream delegate;

        HashingOutputStream(final OutputStream delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public void write(final int b) throws IOException {
            delegate.write(b);
            hasher.putByte((byte) b);
        }

        @Override
        public void write(final byte b[], final int off, final int len) throws IOException {
            delegate.write(b, off, len);
            hasher.putBytes(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        HashCode hash() throws IOException {
            delegate.flush();
            return hasher.hash();
        }
    }
}
