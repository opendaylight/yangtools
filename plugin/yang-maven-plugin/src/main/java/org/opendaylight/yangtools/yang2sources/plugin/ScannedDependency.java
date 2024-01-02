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
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.META_INF_YANG_STRING;
import static org.opendaylight.yangtools.yang2sources.plugin.YangToSourcesProcessor.META_INF_YANG_STRING_JAR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
abstract class ScannedDependency {
    private static final class Single extends ScannedDependency {
        Single(final File file) {
            super(file);
        }

        @Override
        ImmutableList<YangTextSchemaSource> sources() {
            return ImmutableList.of(YangTextSchemaSource.forPath(file().toPath()));
        }
    }

    private static final class Zip extends ScannedDependency {
        private final ImmutableSet<String> entryNames;

        Zip(final File file, final ImmutableSet<String> entryNames) {
            super(file);
            this.entryNames = requireNonNull(entryNames);
        }

        @Override
        ImmutableList<YangTextSchemaSource> sources() throws IOException {
            final var builder = ImmutableList.<YangTextSchemaSource>builderWithExpectedSize(entryNames.size());

            try (ZipFile zip = new ZipFile(file())) {
                for (String entryName : entryNames) {
                    final ZipEntry entry = requireNonNull(zip.getEntry(entryName));

                    builder.add(YangTextSchemaSource.delegateForByteSource(
                        entryName.substring(entryName.lastIndexOf('/') + 1),
                        // FIXME: can we reasonable make this a CharSource?
                        ByteSource.wrap(ByteStreams.toByteArray(zip.getInputStream(entry))),
                        StandardCharsets.UTF_8));
                }
            }

            return builder.build();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ScannedDependency.class);

    private final File file;

    ScannedDependency(final File file) {
        this.file = requireNonNull(file);
    }

    static List<ScannedDependency> scanDependencies(final MavenProject project) throws IOException {
        final List<File> filesOnCp = getClassPath(project);
        LOG.debug("{} Searching for YANG files in dependencies: {}", YangToSourcesProcessor.LOG_PREFIX, filesOnCp);
        LOG.debug("{} Searching for YANG files in {} dependencies", YangToSourcesProcessor.LOG_PREFIX,
            filesOnCp.size());

        final var result = new ArrayList<ScannedDependency>();
        for (File file : filesOnCp) {
            // is it jar file or directory?
            if (file.isDirectory()) {
                final File yangDir = new File(file, META_INF_YANG_STRING);
                if (yangDir.exists() && yangDir.isDirectory()) {
                    result.addAll(scanDirectory(yangDir));
                }
            } else {
                result.addAll(scanZipFile(file));
            }
        }
        return result;
    }

    private static ImmutableList<ScannedDependency> scanDirectory(final File yangDir) {
        return Arrays.stream(yangDir.listFiles(
            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile()))
                .map(Single::new)
                .collect(ImmutableList.toImmutableList());
    }

    private static ImmutableList<ScannedDependency> scanZipFile(final File zipFile) throws IOException {
        final ImmutableSet<String> entryNames;
        try (ZipFile zip = new ZipFile(zipFile)) {
            entryNames = zip.stream()
                .filter(entry -> {
                    final String entryName = entry.getName();
                    return entryName.startsWith(META_INF_YANG_STRING_JAR) && !entry.isDirectory()
                        && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION);
                })
                .map(ZipEntry::getName)
                .collect(ImmutableSet.toImmutableSet());
        }

        return entryNames.isEmpty() ? ImmutableList.of() : ImmutableList.of(new Zip(zipFile, entryNames));
    }

    // FIXME: java.nio.file.Path
    final File file() {
        return file;
    }

    abstract ImmutableList<YangTextSchemaSource> sources() throws IOException;

    @VisibleForTesting
    static List<File> getClassPath(final MavenProject project) {
        return project.getArtifacts().stream()
            .map(Artifact::getFile)
            .filter(file -> file.isFile() && file.getName().endsWith(".jar") || file.isDirectory())
            .toList();
    }
}
