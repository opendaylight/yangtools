/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataConstants;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * An {@link InferenceAction} tasked with identifying when we are dealing with {@link YangDataConstants#RFC8040_SOURCE}.
 */
final class OperationsValidateModuleAction implements InferenceAction {
    private final Prerequisite<? extends Mutable<?, ?, ?>> prereq;

    private OperationsValidateModuleAction(final Prerequisite<? extends Mutable<?, ?, ?>> prereq) {
        this.prereq = requireNonNull(prereq);
    }

    static void applyTo(@NonNull final Mutable<?, ?, ?> module) {
        // Quick checks we can
        if (module.producesDeclared(ModuleStatement.class) && YangDataConstants.MODULE_NAME.equals(module.argument())) {
            // This is 'yang-api' definition within a 'ietf-restconf' module, but we are not certain about revisions
            // and its structure. Next up we require the module to be fully declared, hence an inference action is
            // needed to continue this process.
            final var action = module.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
            final var prereq = action.mutatesEffectiveCtx(module);

            action.apply(new OperationsValidateModuleAction(prereq));
        }
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final Mutable<?, ?, ?> moduleCtx = prereq.resolve(ctx);

        // Check namespace and revision first
        final QNameModule moduleQName = moduleCtx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, moduleCtx);
        if (!YangDataConstants.RFC8040_MODULE.equals(moduleQName)) {
            return;
        }

        // Now carefully locate the operations container:
        //
        //   grouping restconf {
        //     container restconf {
        //       container operations;
        //     }
        //   }
        //
        for (var moduleSub : moduleCtx.mutableDeclaredSubstatements()) {
            if (moduleSub.producesDeclared(GroupingStatement.class) && "restconf".equals(moduleSub.rawArgument())) {
                for (var grpSub : moduleSub.mutableDeclaredSubstatements()) {
                    if (grpSub.producesDeclared(ContainerStatement.class) && "restconf".equals(grpSub.rawArgument())) {
                        for (var contSub : grpSub.mutableDeclaredSubstatements()) {
                            if (contSub.producesDeclared(ContainerStatement.class)
                                && "operations".equals(contSub.rawArgument())) {
                                // Alright, we have a match. Hook the second stage of processing.
                                OperationsCreateLeafStatements.applyTo(moduleCtx, contSub);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // We do not really need to fail, as this means reactor will fail anyway
    }
}
