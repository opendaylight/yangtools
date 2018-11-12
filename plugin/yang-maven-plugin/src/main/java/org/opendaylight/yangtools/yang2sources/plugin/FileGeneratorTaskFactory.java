/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.File;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;

/**
 * Bridge to a {@link FileGenerator} instance.
 *
 * @author Robert Varga
 */
final class FileGeneratorTaskFactory extends GeneratorTaskFactory implements Identifiable<String> {
    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    private FileGeneratorTaskFactory(final FileGenerator gen, final FileGeneratorArg arg) {
        super(gen.importResolutionMode());
        this.arg = arg;
        this.gen = gen;
    }

    static FileGeneratorTaskFactory of(final FileGeneratorFactory factory, final FileGeneratorArg arg)
            throws FileGeneratorException {
        return new FileGeneratorTaskFactory(factory.newFileGenerator(arg.getConfiguration()), arg);
    }

    @Override
    public String getIdentifier() {
        return arg.getIdentifier();
    }

    @Override
    FileGeneratorTask createTask(final MavenProject project, final ContextHolder context) {
        final Build build = project.getModel().getBuild();
        return new FileGeneratorTask(this, context, new File(build.getDirectory()),
            new File(project.getBasedir(), build.getSourceDirectory()).getParentFile());
    }

    @Override
    FileGenerator generator() {
        return gen;
    }

    @Override
    ToStringHelper addToStringProperties(final ToStringHelper helper) {
        return super.addToStringProperties(helper).add("argument", arg);
    }
}
