/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Action execute when the 'uses' parent of 'refine' is fully declared.
 */
final class RefineBaseAction implements InferenceAction {
    // Fully-declared 'refine' statement
    private final @NonNull Mutable<Descendant, RefineStatement, RefineEffectiveStatement> refineStmt;
    private final @NonNull Prerequisite<? extends StmtContext<?, ?, ?>> usesPrereq;
    private final @NonNull StmtContext<?, ?, ?> refineBase;

    RefineBaseAction(final Mutable<Descendant, RefineStatement, RefineEffectiveStatement> refineStmt,
            final StmtContext<?, ?, ?> refineBase, final Prerequisite<? extends StmtContext<?, ?, ?>> usesPrereq) {
        this.refineStmt = requireNonNull(refineStmt);
        this.refineBase = requireNonNull(refineBase);
        this.usesPrereq = requireNonNull(usesPrereq);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var usesStmt = usesPrereq.resolve(ctx);
        if (!usesStmt.isSupportedByFeatures()) {
            // 'uses' is not supported: there are no further effects
            return;
        }
        if (!refineStmt.isSupportedByFeatures()) {
            // 'refine' is not supported: there are no further effects
            // FIXME: there is just no base for this behaviour, in RFC7950 but this exists since YANGTOOLS-666
            refineStmt.setUnsupported();
            return;
        }


        // Second step: we are blocking the descendant of refineBase from becoming effective before we become effective
        final var action = refineStmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var targetPrereq = action.mutatesEffectiveCtxPath(refineBase, ParserNamespaces.schemaTree(),
            refineStmt.getArgument().getNodeIdentifiers());
        action.apply(new RefineTargetAction(refineStmt, targetPrereq));
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // No-op, we are failing anyway
    }
}
