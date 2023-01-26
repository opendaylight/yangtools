/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.sonatype.plexus.build.incremental.BuildContext;

@NonNullByDefault
abstract class GeneratorTask<T extends GeneratorTaskFactory> extends ParserConfigAware {
    private final ContextHolder context;
    private final T factory;

    GeneratorTask(final T factory, final ContextHolder context) {
        this.factory = requireNonNull(factory);
        this.context = requireNonNull(context);
    }

    @Override
    final YangParserConfiguration parserConfig() {
        return factory.parserConfig();
    }

    final List<FileHash> execute(final BuildContext buildContext) throws FileGeneratorException, IOException {
        return execute(factory, context, buildContext);
    }

    abstract List<FileHash> execute(T factory, ContextHolder modelContext, BuildContext buildContext)
        throws FileGeneratorException, IOException;
}
