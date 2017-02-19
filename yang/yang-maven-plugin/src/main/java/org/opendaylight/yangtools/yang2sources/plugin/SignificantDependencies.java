/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_FILE_EXTENSION;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SignificantDependencies implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SignificantDependencies.class);
    private static final SignificantDependencies EMPTY = new SignificantDependencies(ImmutableList.of());

    private final List<SignificantDependency> dependencies;

    SignificantDependencies(final List<SignificantDependency> dependencies) {
        this.dependencies = ImmutableList.copyOf(dependencies);
    }

    static SignificantDependencies ofProject(final MavenProject project, final boolean scan)
            throws MojoExecutionException {
        if (!scan) {
            return EMPTY;
        }

        final List<File> filesOnCp;
        try {
            filesOnCp = Util.getClassPath(project);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to scan for YANG files in dependencies", e);
        }

        LOG.info("{} Searching for yang files in following dependencies: {}", YangToSourcesProcessor.LOG_PREFIX,
            filesOnCp);

        final List<SignificantDependency> dependencies = new ArrayList<>();
        for (File file : filesOnCp) {
            if (file.isDirectory()) {
                final File yangDir = new File(file, YangToSourcesProcessor.META_INF_YANG_STRING);
                if (yangDir.exists() && yangDir.isDirectory()) {
                    final File[] yangFiles = yangDir.listFiles(
                        (dir, name) -> name.endsWith(RFC6020_YANG_FILE_EXTENSION) && new File(dir, name).isFile());
                    for (File f : yangFiles) {
                        dependencies.add(new FileSignificantDependency(f));
                    }
                }

                continue;
            }

            final Optional<ZipSignificantDependency> opt = ZipSignificantDependency.create(file);
            if (opt.isPresent()) {
                dependencies.add(opt.get());
            }
        }

        return dependencies.isEmpty() ? EMPTY : new SignificantDependencies(dependencies);
    }

    Collection<File> asFiles() {
        return Collections2.transform(dependencies, SignificantDependency::file);
    }

    boolean isEmpty() {
        return dependencies.isEmpty();
    }

    Collection<? extends InputStream> toStreamsWithoutDuplicates() throws IOException {
        final Map<String, ByteSource> byContent = new HashMap<>();
        for (SignificantDependency d : dependencies) {
            for (ByteSource s : d.asSources()) {
                try (Reader reader = s.asCharSource(StandardCharsets.UTF_8).openStream()) {
                    final String contents = CharStreams.toString(reader);
                    byContent.putIfAbsent(contents, s);
                }
            }
        }

        final List<InputStream> inputs = new ArrayList<>(byContent.size());
        for (ByteSource entry : byContent.values()) {
            inputs.add(entry.openStream());
        }
        return inputs;

    }

    @Override
    public void close() {
        dependencies.forEach(SignificantDependency::close);
    }
}
