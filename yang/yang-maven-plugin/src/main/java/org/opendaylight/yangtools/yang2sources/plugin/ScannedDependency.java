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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
abstract class ScannedDependency {
    private static final class Single extends ScannedDependency {

        Single(final File file) {
            super(file);
        }

        @Override
        Collection<YangTextSchemaSource> sources() {
            return ImmutableList.of(YangTextSchemaSource.forFile(file()));
        }
    }

    private static final class Zip extends ScannedDependency {
        private final Set<String> entryNames;

        Zip(final File file, final Collection<String> entryNames) {
            super(file);
            this.entryNames = ImmutableSet.copyOf(entryNames);
        }

        @Override
        Collection<YangTextSchemaSource> sources() throws IOException {
            final Collection<YangTextSchemaSource> result = new ArrayList<>(entryNames.size());

            try (ZipFile zip = new ZipFile(file())) {
                for (String entryName : entryNames) {
                    final ZipEntry entry = requireNonNull(zip.getEntry(entryName));

                    result.add(YangTextSchemaSource.delegateForByteSource(
                        entryName.substring(entryName.lastIndexOf('/') + 1),
                        ByteSource.wrap(ByteStreams.toByteArray(zip.getInputStream(entry)))));
                }
            }

            return result;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ScannedDependency.class);

    private final File file;

    ScannedDependency(final File file) {
        this.file = requireNonNull(file);
    }

    static Collection<ScannedDependency> scanDependencies(final MavenProject project) throws IOException {
        final Collection<File> filesOnCp = Util.getClassPath(project);
        LOG.debug("{} Searching for YANG files in dependencies: {}", YangToSourcesProcessor.LOG_PREFIX, filesOnCp);
        LOG.debug("{} Searching for YANG files in {} dependencies", YangToSourcesProcessor.LOG_PREFIX,
            filesOnCp.size());

        final List<ScannedDependency> result = new ArrayList<>();
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

    private static Collection<ScannedDependency> scanDirectory(final File yangDir) {
        return Arrays.stream(yangDir.listFiles(
            (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile()))
                .map(Single::new).collect(ImmutableList.toImmutableList());
    }

    private static Collection<ScannedDependency> scanZipFile(final File zipFile) throws IOException {
        final Collection<String> entryNames;
        try (ZipFile zip = new ZipFile(zipFile)) {
            entryNames = zip.stream().filter(entry -> {
                final String entryName = entry.getName();
                return entryName.startsWith(META_INF_YANG_STRING_JAR) && !entry.isDirectory()
                        && entryName.endsWith(RFC6020_YANG_FILE_EXTENSION);
            }).map(ZipEntry::getName).collect(ImmutableList.toImmutableList());
        }

        return entryNames.isEmpty() ? ImmutableList.of() : ImmutableList.of(new Zip(zipFile, entryNames));
    }

    final File file() {
        return file;
    }

    abstract Collection<YangTextSchemaSource> sources() throws IOException;
}
