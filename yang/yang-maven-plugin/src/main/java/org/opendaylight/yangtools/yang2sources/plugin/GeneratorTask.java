/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.plugin.generator.api.FileGenerator.ImportResolutionMode;
import org.opendaylight.yangtools.plugin.generator.api.FileGeneratorException;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.sonatype.plexus.build.incremental.BuildContext;

@NonNullByDefault
abstract class GeneratorTask {
    private final StatementParserMode parserMode;

    GeneratorTask(final ImportResolutionMode importMode) {
        switch (importMode) {
            case REVISION_EXACT_OR_LATEST:
                parserMode = StatementParserMode.DEFAULT_MODE;
                break;
            case SEMVER_LATEST:
                parserMode = StatementParserMode.SEMVER_MODE;
                break;
            default:
                throw new LinkageError("Unhandled import mode " + importMode);
        }
    }

    final StatementParserMode parserMode() {
        return parserMode;
    }

    abstract void initialize(MavenProject project, ContextHolder context);

    abstract Collection<File> execute(BuildContext buildContext) throws FileGeneratorException, IOException;
}
