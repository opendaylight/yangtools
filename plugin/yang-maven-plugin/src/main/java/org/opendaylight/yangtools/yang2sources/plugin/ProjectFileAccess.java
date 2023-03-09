/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.HashSet;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object mediating access to the project directory.
 */
final class ProjectFileAccess {
    private record ProjectDirectory(@NonNull File dir, @NonNull HashSet<File> files) {
        private ProjectDirectory {
            requireNonNull(dir);
            requireNonNull(files);
        }

        ProjectDirectory(final @NonNull File dir) {
            this(dir, new HashSet<>());
        }

        @NonNull File add(final String relativePath) {
            final var ret = new File(dir, relativePath);
            files.add(ret);
            return ret;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ProjectFileAccess.class);

    private final @NonNull MavenProject project;
    private final @NonNull String buildDirSuffix;

    private ProjectDirectory sourceDir;
    private ProjectDirectory resourceDir;
    private ProjectDirectory buildSourceDir;
    private ProjectDirectory buildResourceDir;

    ProjectFileAccess(final MavenProject project, final String buildDirSuffix) {
        this.project = requireNonNull(project);
        this.buildDirSuffix = requireNonNull(buildDirSuffix);
    }

    @Nullable File persistentFile(final GeneratedFileType fileType, final String relativePath)
            throws FileGeneratorException {
        final ProjectDirectory fileDirectory;
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            var local = sourceDir;
            if (local == null) {
                sourceDir = local = new ProjectDirectory(new File(project.getBuild().getSourceDirectory()));
            }
            fileDirectory = local;
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            var local = resourceDir;
            if (local == null) {
                resourceDir = local =
                    new ProjectDirectory(new File(new File(project.getBuild().getSourceDirectory()).getParentFile(),
                        "resources"));
            }
            fileDirectory = local;
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }

        final var ret = fileDirectory.add(relativePath);
        if (ret.exists()) {
            LOG.debug("{}: Skipping existing persistent {}", YangToSourcesProcessor.LOG_PREFIX, ret);
            return null;
        }
        return ret;
    }

    @NonNull File transientFile(final GeneratedFileType fileType, final String relativePath)
            throws FileGeneratorException {
        final ProjectDirectory fileDirectory;
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            var local = buildSourceDir;
            if (local == null) {
                buildSourceDir = local = buildDirectoryFor("generated-sources");
            }
            fileDirectory = local;
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            var local = buildResourceDir;
            if (local == null) {
                buildResourceDir = local = buildDirectoryFor("generated-resources");
            }
            fileDirectory = local;
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }

        return fileDirectory.add(relativePath);
    }

    void updateMavenProject() {
        if (buildSourceDir != null) {
            project.addCompileSourceRoot(buildSourceDir.dir().getPath());
        }
        if (buildResourceDir != null) {
            addResourceDir(project, buildResourceDir.dir().getPath());
        }
    }

    static void addResourceDir(final MavenProject project, final String dir) {
        var res = new Resource();
        res.setDirectory(requireNonNull(dir));
        project.addResource(res);
    }

    private @NonNull ProjectDirectory buildDirectoryFor(final String name) {
        return new ProjectDirectory(
            IncrementalBuildSupport.pluginSubdirectory(project.getBuild().getDirectory(), buildDirSuffix, name)
                .toFile());
    }
}
