/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.META_INF_STR;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.YANG_STR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.DelegatedYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.FileYangTextSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
abstract sealed class ScannedDependency {
    private static final class Single extends ScannedDependency {
        Single(final Path file) {
            super(file);
        }

        @Override
        List<YangTextSource> sources() {
            return List.of(new FileYangTextSource(file()));
        }
    }

    private static final class Zip extends ScannedDependency {
        private final List<Path> files;

        Zip(final Path file, final List<Path> files) {
            super(file);
            this.files = List.copyOf(files);
        }

        @Override
        List<YangTextSource> sources() throws IOException {
            final var tmp = new ArrayList<YangTextSource>(files.size());
            try (var zipfs = FileSystems.newFileSystem(file())) {
                final var root = zipfs.getPath("/");
                for (var file : files) {
                    tmp.add(new DelegatedYangTextSource(
                        SourceIdentifier.ofYangFileName(file.getFileName().toString()),
                        ByteSource.wrap(Files.readAllBytes(root.resolve(file))).asCharSource(StandardCharsets.UTF_8)));
                }
            }
            return List.copyOf(tmp);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ScannedDependency.class);

    private final Path file;

    ScannedDependency(final Path file) {
        this.file = requireNonNull(file);
    }

    static List<ScannedDependency> scanDependencies(final MavenProject project) throws IOException {
        final var filesOnCp = getClassPath(project);
        LOG.debug("{} Searching for YANG files in dependencies: {}", YangToSourcesProcessor.LOG_PREFIX, filesOnCp);
        LOG.debug("{} Searching for YANG files in {} dependencies", YangToSourcesProcessor.LOG_PREFIX,
            filesOnCp.size());

        final var result = new ArrayList<ScannedDependency>();
        for (var file : filesOnCp) {
            final var path = file.toPath();

            // is it a directory?
            if (Files.isDirectory(path)) {
                // FIXME: YANGTOOLS-1693: java.nio.file.Files instead
                final var yangDir = path.resolve(META_INF_STR).resolve(YANG_STR);
                if (Files.isDirectory(yangDir)) {
                    result.addAll(scanDirectory(yangDir));
                }
            } else if (Files.isRegularFile(path)) {
                // is it a jar file?
                result.addAll(scanZipFile(path));
            }
        }
        return result;
    }

    private static List<ScannedDependency> scanDirectory(final Path yangDir) {
        // FIXME: YANGTOOLS-1693: java.nio.file.Files instead
        return Arrays.stream(yangDir.toFile().listFiles(
            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile()))
                .map(file -> new Single(file.toPath()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<ScannedDependency> scanZipFile(final Path zipFile) throws IOException {
        final List<Path> files;
        try (var zipfs = FileSystems.newFileSystem(zipFile)) {
            final var root = zipfs.getPath("/");
            final var metaInfYang = root.resolve(META_INF_STR).resolve(YANG_STR);
            if (!Files.isDirectory(metaInfYang)) {
                return List.of();
            }

            try (var wlk = Files.walk(metaInfYang, 1)) {
                files = wlk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(RFC6020_YANG_FILE_EXTENSION))
                    .map(root::relativize)
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
            }
        }
        return files.isEmpty() ? List.of() : List.of(new Zip(zipFile, files));
    }

    final Path file() {
        return file;
    }

    abstract List<YangTextSource> sources() throws IOException;

    @VisibleForTesting
    static List<File> getClassPath(final MavenProject project) {
        return project.getArtifacts().stream()
            .map(Artifact::getFile)
            .filter(file -> file.isFile() && file.getName().endsWith(".jar") || file.isDirectory())
            .toList();
    }
}
