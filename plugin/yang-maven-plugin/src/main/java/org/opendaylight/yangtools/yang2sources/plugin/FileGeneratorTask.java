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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
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

    private final Map<GeneratedFileType, File> persistentDirs = new HashMap<>(4);
    private final Map<GeneratedFileType, File> transientDirs = new HashMap<>(4);
    private final MavenProject project;
    private final File buildDir;
    private final String suffix;

    FileGeneratorTask(final @NonNull FileGeneratorTaskFactory factory, final @NonNull ContextHolder context,
            final MavenProject project) {
        super(factory, context);

        final Build build = project.getBuild();
        final String buildDirectory = build.getDirectory();
        this.buildDir = new File(buildDirectory);
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
        final File existing = persistentDirs.get(fileType);
        if (existing != null) {
            return existing;
        }
        final File newDir = persistentDirectory(fileType);
        verify(persistentDirs.put(fileType, newDir) == null);
        return newDir;
    }

    private File transientPath(final GeneratedFileType fileType) throws FileGeneratorException {
        final File existing = transientDirs.get(fileType);
        if (existing != null) {
            return existing;
        }

        final File newDir = transientDirectory(fileType);
        verify(transientDirs.put(fileType, newDir) == null);
        return newDir;
    }

    private File persistentDirectory(final GeneratedFileType fileType) throws FileGeneratorException {
        final File ret;
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            ret = new File(project.getBuild().getSourceDirectory());
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            ret = new File(new File(project.getBuild().getSourceDirectory()).getParentFile(), "resources");
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }
        return ret;
    }

    private File transientDirectory(final GeneratedFileType fileType) throws FileGeneratorException {
        final File ret;
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            ret = transientDirectory("generated-sources");
            project.addCompileSourceRoot(ret.toString());
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            ret = transientDirectory("generated-resources");
            project.addResource(createResouce(ret));
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }
        return ret;
    }

    private File transientDirectory(final String component) {
        return new File(buildDir, subdirFileName(component));
    }

    private String subdirFileName(final String component) {
        return component + File.separatorChar + suffix;
    }

    private static Resource createResouce(final File directory) {
        final Resource ret = new Resource();
        ret.setDirectory(directory.toString());
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
        private final Hasher hasher = HashedFile.HASH_FUNCTION.newHasher();
        private final OutputStream delegate;

        HashingOutputStream(final OutputStream delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public void write(final int b) throws IOException {
            delegate.write(b);
            hasher.putByte((byte) b);
        }

        @Override
        @SuppressWarnings("checkstyle:parameterName")
        public void write(final byte[] b, final int off, final int len) throws IOException {
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
