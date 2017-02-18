/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Table;
import java.util.Optional;
import org.apache.maven.project.MavenProject;
import org.opendaylight.yangtools.yang.maven.spi.generator.FileGenerator;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFile;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFileKind;
import org.opendaylight.yangtools.yang.maven.spi.generator.GeneratedFilePath;
import org.opendaylight.yangtools.yang.maven.spi.generator.ImportResolutionMode;
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
        this.gen = requireNonNull(gen);
    }

    @Override
    void setBuildContext(final BuildContext buildContext) {
        // Intentional no-op
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder context) {
        this.context = requireNonNull(context);

        if (gen instanceof MavenProjectAware) {
            ((MavenProjectAware)gen).setMavenProject(project);
        }
    }

    @Override
    Optional<ImportResolutionMode> suggestedImportResolutionMode() {
        return gen.suggestedImportResolutionMode();
    }

    @Override
    boolean isAcceptableImportResolutionMode(final ImportResolutionMode mode) {
        return gen.isAcceptableImportResolutionMode(mode);
    }

    @Override
    public GeneratorResult call() throws Exception {
        final Table<GeneratedFileKind, GeneratedFilePath, GeneratedFile> generatedFiles = gen.generateFiles(
            context.getContext(), context.getYangModules(), context::moduleToGeneratedFilePath);

        // TODO Auto-generated method stub
        return null;
    }
}
