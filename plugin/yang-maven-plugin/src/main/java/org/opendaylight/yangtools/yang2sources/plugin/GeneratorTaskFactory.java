/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorFactory;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

@NonNullByDefault
final class GeneratorTaskFactory extends ParserConfigAware implements Identifiable<String> {
    private final YangParserConfiguration parserConfig;
    private final FileGeneratorArg arg;
    private final FileGenerator gen;

    private GeneratorTaskFactory(final FileGenerator gen, final FileGeneratorArg arg) {
        this.gen = requireNonNull(gen);
        this.arg = requireNonNull(arg);
        parserConfig = switch (gen.importResolutionMode()) {
            case REVISION_EXACT_OR_LATEST -> YangParserConfiguration.DEFAULT;
        };
    }

    static GeneratorTaskFactory of(final FileGeneratorFactory factory, final FileGeneratorArg arg)
            throws FileGeneratorException {
        return new GeneratorTaskFactory(factory.newFileGenerator(arg.getConfiguration()), arg);
    }

    @Override
    public String getIdentifier() {
        return arg.getIdentifier();
    }

    @Override
    YangParserConfiguration parserConfig() {
        return parserConfig;
    }

    FileGenerator generator() {
        return gen;
    }

    String generatorName() {
        return gen.getClass().getName();
    }

    /**
     * Create a new {@link GeneratorTask} which will work in scope of specified {@link MavenProject} with the effective
     * model held in specified {@link ContextHolder}.
     *
     * @param project current Maven Project
     * @param context model generation context
     */
    GeneratorTask createTask(final MavenProject project, final ContextHolder context) {
        return new GeneratorTask(this, context, project);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("generator", generatorName()).add("argument", arg).toString();
    }
}
