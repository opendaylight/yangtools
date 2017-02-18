/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.Preconditions;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.maven.spi.generator.FileGenerator;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A generator task performed using {@link FileGenerator}.
 *
 * @author Robert Varga
 */
final class FileGeneratorTask extends AbstractGeneratorTask {
    private final FileGenerator gen;

    private ContextHolder context;

    FileGeneratorTask(final FileGenerator gen) {
        this.gen = Preconditions.checkNotNull(gen);
    }

    @Override
    void setBuildContext(final BuildContext buildContext) {
        // Intentional no-op
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder context) {
        this.context = Preconditions.checkNotNull(context);

        if (gen instanceof MavenProjectAware) {
            ((MavenProjectAware)gen).setMavenProject(project);
        }
    }

    @Override
    public GeneratorResult call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
