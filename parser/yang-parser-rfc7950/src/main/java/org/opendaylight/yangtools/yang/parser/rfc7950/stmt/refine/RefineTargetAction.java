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
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link InferenceAction} executed when the {@code refine} target is resolve. Deals with editing the target node with
 * refine contents. Should the target not be supported it will disable the refine statement.
 */
final class RefineTargetAction implements InferenceAction {
    private static final Logger LOG = LoggerFactory.getLogger(RefineTargetAction.class);

    private final @NonNull Mutable<Descendant, RefineStatement, RefineEffectiveStatement> refineStmt;
    private final @NonNull Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> targetPrereq;

    RefineTargetAction(final Mutable<Descendant, RefineStatement, RefineEffectiveStatement> refineStmt,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> targetPrereq) {
        this.refineStmt = requireNonNull(refineStmt);
        this.targetPrereq = requireNonNull(targetPrereq);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        final var refineTargetNodeCtx = targetPrereq.resolve(ctx);
        if (!refineTargetNodeCtx.isSupportedByFeatures()) {
            refineStmt.setUnsupported();
            return;
        }

        // FIXME: This communicates the looked-up target node to RefineStatementSupport.buildEffective(). We should do
        //        this trick through a shared namespace or similar reactor-agnostic meeting place. It really feels like
        //        an inference action RefineStatementSupport should be doing.
        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.trace("Refine node '{}' in uses '{}' has target node unknown statement '{}'. "
                + "Refine has been skipped. At line: {}", refineStmt.argument(),
                refineStmt.coerceParentContext().argument(), refineTargetNodeCtx.argument(),
                refineStmt.sourceReference());
            return;
        }

        for (var refineSubstatementCtx : refineStmt.declaredSubstatements()) {
            if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                addOrReplaceNode(refineSubstatementCtx, refineTargetNodeCtx);
            }
        }

        refineStmt.addToNs(RefineTargetNamespace.INSTANCE, Empty.value(), refineTargetNodeCtx);
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        throw new InferenceException(refineStmt, "Refine target node %s not found.", refineStmt.getArgument());
    }

    @Override
    public void prerequisiteUnavailable(final Prerequisite<?> unavail) {
        // Target is a prerequisite for the 'refine', hence if the target is not supported, the refine is not supported
        // as well.
        refineStmt.setUnsupported();
    }

    private static void addOrReplaceNode(final StmtContext<?, ?, ?> refineSubstatementCtx,
            final Mutable<?, ?, ?> refineTargetNodeCtx) {
        final var refineSubstatementDef = refineSubstatementCtx.publicDefinition();

        // FIXME: this is quite costly, use an explicit block
        SourceException.throwIf(!isSupportedRefineTarget(refineSubstatementCtx, refineTargetNodeCtx),
            refineSubstatementCtx,
            "Error in module '%s' in the refine of uses '%s': can not perform refine of '%s' for the target '%s'.",
            refineSubstatementCtx.getRoot().rawArgument(), refineSubstatementCtx.coerceParentContext().argument(),
            refineSubstatementCtx.publicDefinition(), refineTargetNodeCtx.publicDefinition());

        if (!isAllowedToAddByRefine(refineSubstatementDef)) {
            refineTargetNodeCtx.removeStatementFromEffectiveSubstatements(refineSubstatementDef);
        }
        // FIXME: childCopyOf() should handle this through per-statement copy policy, right?
        refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx.replicaAsChildOf(refineTargetNodeCtx));
    }

    // FIXME: clarify this and inline into single caller
    private static boolean isAllowedToAddByRefine(final StatementDefinition publicDefinition) {
        return YangStmtMapping.MUST.equals(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(final StmtContext<?, ?, ?> refineSubstatementCtx) {
        final Collection<?> supportedRefineSubstatements = refineSubstatementCtx.namespaceItem(
                ValidationBundles.NAMESPACE, ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

        return supportedRefineSubstatements == null || supportedRefineSubstatements.isEmpty()
                || supportedRefineSubstatements.contains(refineSubstatementCtx.publicDefinition())
                || StmtContextUtils.isUnknownStatement(refineSubstatementCtx);
    }

    private static boolean isSupportedRefineTarget(final StmtContext<?, ?, ?> refineSubstatementCtx,
            final StmtContext<?, ?, ?> refineTargetNodeCtx) {
        final var supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS.get(
            refineSubstatementCtx.publicDefinition());

        return supportedRefineTargets == null || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx.publicDefinition());
    }
}
