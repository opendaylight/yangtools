/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import org.apache.maven.project.MavenProject;

/**
 * Utility to obtain the correct path to generated directories.
 * It's important that this does not hard-code "target/" anywhere, but uses
 * ${project.build.directory}/, to make target-ide/ possible.
 *
 * @author Michael Vorburger.ch
 */
class GeneratedDirectories {

    private final File targetGeneratedSources;

    GeneratedDirectories(MavenProject project) {
        this.targetGeneratedSources = new File(project.getBuild().getDirectory(), "generated-sources");
    }

    public File getYangServicesDir() {
        return new File(targetGeneratedSources, "spi");
    }

    public File getYangDir() {
        return new File(targetGeneratedSources, "yang");
    }

}
