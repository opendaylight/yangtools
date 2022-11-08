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
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileType;

/**
 * An object mediating access to the project directory.
 */
final class ProjectFileAccess {
    private final MavenProject project;
    private final String buildDirSuffix;

    private @Nullable File sourceDir;
    private @Nullable File resourceDir;
    private @Nullable File buildSourceDir;
    private @Nullable File buildResourceDir;

    ProjectFileAccess(final MavenProject project, final String buildDirSuffix) {
        this.project = requireNonNull(project);
        this.buildDirSuffix = requireNonNull(buildDirSuffix);
    }

    @NonNull File persistentPath(final GeneratedFileType fileType) throws FileGeneratorException {
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            var local = sourceDir;
            if (local == null) {
                sourceDir = local = new File(project.getBuild().getSourceDirectory());
            }
            return local;
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            var local = resourceDir;
            if (local == null) {
                resourceDir = local = new File(new File(project.getBuild().getSourceDirectory()).getParentFile(),
                    "resources");
            }
            return local;
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }
    }

    @NonNull File transientPath(final GeneratedFileType fileType) throws FileGeneratorException {
        if (GeneratedFileType.SOURCE.equals(fileType)) {
            var local = buildSourceDir;
            if (local == null) {
                buildSourceDir = local = buildDirectoryFor("generated-sources");
            }
            return local;
        } else if (GeneratedFileType.RESOURCE.equals(fileType)) {
            var local = buildResourceDir;
            if (local == null) {
                buildResourceDir = local = buildDirectoryFor("generated-resources");
            }
            return local;
        } else {
            throw new FileGeneratorException("Unknown generated file type " + fileType);
        }
    }

    void updateMavenProject() {
        var local = buildSourceDir;
        if (local != null) {
            project.addCompileSourceRoot(local.getPath());
        }
        local = buildResourceDir;
        if (local != null) {
            addResourceDir(project, local);
        }
    }

    static void addResourceDir(final MavenProject project, final File dir) {
        var res = new Resource();
        res.setDirectory(dir.getPath());
        project.addResource(res);
    }

    private @NonNull File buildDirectoryFor(final String name) {
        return IncrementalBuildSupport.pluginSubdirectory(project.getBuild().getDirectory(), buildDirSuffix, name)
            .toFile();
    }
}
