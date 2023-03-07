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

    @NonNull File accessSourceDir() {
        var local = sourceDir;
        if (local == null) {
            sourceDir = local = new File(project.getBuild().getSourceDirectory());
        }
        return local;
    }

    @NonNull File accessResourceDir() {
        var local = resourceDir;
        if (local == null) {
            resourceDir = local = new File(new File(project.getBuild().getSourceDirectory()).getParentFile(),
                "resources");
        }
        return local;
    }

    @NonNull File accessBuildSourceDir() {
        var local = buildSourceDir;
        if (local == null) {
            buildSourceDir = local = buildDirectoryFor("generated-sources");
        }
        return local;
    }

    @NonNull File accessBuildResourceDir() {
        var local = buildResourceDir;
        if (local == null) {
            buildResourceDir = local = buildDirectoryFor("generated-resources");
        }
        return local;
    }

    void updateMavenProject() {
        var local = buildSourceDir;
        if (local != null) {
            project.addCompileSourceRoot(local.getPath());
        }
        local = buildResourceDir;
        if (local != null) {
            final var resource = new Resource();
            resource.setDirectory(local.toString());
            project.addResource(resource);
        }
    }

    private @NonNull File buildDirectoryFor(final String name) {
        return new File(project.getBuild().getDirectory()
            + File.separatorChar + name
            + File.separatorChar + buildDirSuffix);
    }
}
