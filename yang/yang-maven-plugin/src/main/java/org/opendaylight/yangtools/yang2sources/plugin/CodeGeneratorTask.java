/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang2sources.plugin.ConfigArg.CodeGeneratorArg;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator;
import org.opendaylight.yangtools.yang2sources.spi.BasicCodeGenerator.ImportResolutionMode;
import org.opendaylight.yangtools.yang2sources.spi.BuildContextAware;
import org.opendaylight.yangtools.yang2sources.spi.MavenProjectAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Bridge to legacy {@link BasicCodeGenerator} generation.
 *
 * @author Robert Varga
 */
final class CodeGeneratorTask extends GeneratorTask {
    private static final Logger LOG = LoggerFactory.getLogger(CodeGeneratorTask.class);

    private final BasicCodeGenerator gen;
    private final CodeGeneratorArg cfg;

    private ContextHolder context;
    private File outputDir;

    private CodeGeneratorTask(final ImportResolutionMode importMode, final BasicCodeGenerator gen,
            final CodeGeneratorArg cfg) {
        super(importMode.toFileGeneratorMode());
        this.gen = requireNonNull(gen);
        this.cfg = requireNonNull(cfg);
    }

    static GeneratorTask create(final CodeGeneratorArg cfg) throws MojoFailureException {
        cfg.check();

        final String codegenClass = cfg.getCodeGeneratorClass();
        final Class<?> clazz;
        try {
            clazz = Class.forName(codegenClass);
        } catch (ClassNotFoundException e) {
            throw new MojoFailureException("Failed to find code generator class " + codegenClass, e);
        }
        final Class<? extends BasicCodeGenerator> typedClass;
        try {
            typedClass = clazz.asSubclass(BasicCodeGenerator.class);
        } catch (ClassCastException e) {
            throw new MojoFailureException("Code generator " + clazz + " does not implement "
                + BasicCodeGenerator.class, e);
        }
        final Constructor<? extends BasicCodeGenerator> ctor;
        try {
            ctor = typedClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new MojoFailureException("Code generator " + clazz
                + " does not have an accessible no-argument constructor", e);
        }
        final @NonNull BasicCodeGenerator gen;
        try {
            gen = ctor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new MojoFailureException("Failed to instantiate code generator " + clazz, e);
        }
        LOG.debug("{} Code generator instantiated from {}", codegenClass);

        ImportResolutionMode importMode = gen.getImportResolutionMode();
        if (importMode == null) {
            importMode = ImportResolutionMode.REVISION_EXACT_OR_LATEST;
        }
        return new CodeGeneratorTask(importMode, gen, cfg);
    }

    @Override
    void initialize(final MavenProject project, final ContextHolder newCcontext) {
        this.context = requireNonNull(context);

        if (gen instanceof MavenProjectAware) {
            ((MavenProjectAware)gen).setMavenProject(project);
        }

        LOG.debug("Project root dir is {}", project.getBasedir());
        LOG.debug("Additional configuration picked up for : {}: {}", gen.getClass(), cfg.getAdditionalConfiguration());

        outputDir = cfg.getOutputBaseDir(project);
        project.addCompileSourceRoot(outputDir.getAbsolutePath());

        gen.setAdditionalConfig(cfg.getAdditionalConfiguration());
        File resourceBaseDir = cfg.getResourceBaseDir(project);

        final Resource res = new Resource();
        res.setDirectory(resourceBaseDir.getPath());
        project.addResource(res);

        gen.setResourceBaseDir(resourceBaseDir);
        LOG.debug("Folder: {} marked as resources for generator: {}", resourceBaseDir, gen.getClass());
    }

    @Override
    Collection<File> execute(final BuildContext buildContext) throws IOException {
        final Stopwatch watch = Stopwatch.createStarted();
        final boolean mark;
        if (gen instanceof BuildContextAware) {
            ((BuildContextAware)gen).setBuildContext(buildContext);
            mark = false;
        } else {
            mark = true;
        }

        LOG.debug("Sources will be generated to {}", outputDir);
        Collection<File> sources = gen.generateSources(context.getContext(), outputDir, context.getYangModules(),
            context);
        if (sources == null) {
            sources = List.of();
        }

        LOG.debug("{} Sources generated by {}: {}", gen.getClass(), sources);
        LOG.info("Sources generated by {}: {} in {}", cfg.getCodeGeneratorClass(), sources.size(), watch);

        if (mark) {
            sources.forEach(buildContext::refresh);
        }

        return sources;
    }
}
