/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inference action, split out of {@link AbstractAugmentStatementSupport} for clarity and potential specialization.
 */
final class AugmentInferenceAction implements InferenceAction {
    private static final Logger LOG = LoggerFactory.getLogger(AugmentInferenceAction.class);

    private final @NonNull Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode;
    private final @NonNull Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target;
    private final @NonNull AbstractAugmentStatementSupport statementSupport;

    private boolean targetUnavailable;

    AugmentInferenceAction(final AbstractAugmentStatementSupport statementSupport,
            final Mutable<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> augmentNode,
            final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> target) {
        this.statementSupport = requireNonNull(statementSupport);
        this.augmentNode = requireNonNull(augmentNode);
        this.target = requireNonNull(target);
    }

    @Override
    public void apply(final InferenceContext ctx) {
        if (targetUnavailable) {
            // Target node is not available, no further processing and the augment should not leak into effective world
            augmentNode.setUnsupported();
            return;
        }

        final var augmentTargetCtx = target.resolve(ctx);
        if (!isSupportedAugmentTarget(augmentTargetCtx)
            || AbstractAugmentStatementSupport.isInExtensionBody(augmentTargetCtx)) {
            augmentNode.setUnsupported();
            return;
        }

        // We are targeting a context which is creating implicit nodes. In order to keep things consistent,
        // we will need to circle back when creating effective statements.
        if (augmentTargetCtx.hasImplicitParentSupport()) {
            augmentNode.addToNs(AugmentImplicitHandlingNamespace.INSTANCE, Empty.value(), augmentTargetCtx);
        }

        strategyFor(augmentTargetCtx).copyFromSourceToTarget(augmentNode, augmentTargetCtx);
        augmentTargetCtx.addEffectiveSubstatement(augmentNode.replicaAsChildOf(augmentTargetCtx));
    }

    @NonNullByDefault
    private AugmentStrategy strategyFor(final StmtContext<?, ?, ?> targetNode) {
        final var augmentParent = augmentNode.coerceParentContext();

        // 'augment' statement in a 'uses' statement
        if (augmentParent.produces(UsesStatement.DEF)) {
            return AugmentStrategy.USES;
        }

        // 'augment' statement in a 'module' or 'submodule', with target node being ...
        return augmentParent.currentModule().equals(targetNode.currentModule())
            // ... in the same module
            ? AugmentStrategy.SAME_MODULE
            // ... in another module (but perhaps introduced by this module)
            : statementSupport.strategyFor(augmentNode);
    }

    @Override
    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
        // do not fail, if it is an uses-augment to an unknown node
        if (augmentNode.coerceParentContext().produces(UsesStatement.DEF)) {
            if (!augmentNode.isSupportedToBuildEffective()) {
                // We are not supported, hence the uses is not effective and we should bail
                return;
            }

            final var augmentArg = augmentNode.getArgument();
            final var targetNode = ParserNamespaces.findSchemaTreeStatement(
                AbstractAugmentStatementSupport.getSearchRoot(augmentNode), augmentArg);
            if (targetNode.isPresent() && targetNode.orElseThrow().producesExtension()) {
                augmentNode.setUnsupported();
                LOG.warn("Uses-augment to unknown node {}. Augmentation has not been performed. At line: {}",
                    augmentArg, augmentNode.sourceReference());
                return;
            }
        }

        throw new InferenceException(augmentNode, "Augment target '%s' not found", augmentNode.argument());
    }

    @Override
    public void prerequisiteUnavailable(final Prerequisite<?> unavail) {
        if (target.equals(unavail)) {
            targetUnavailable = true;
        } else {
            prerequisiteFailed(List.of(unavail));
        }
    }

    private static boolean isSupportedAugmentTarget(final StmtContext<?, ?, ?> substatementCtx) {
        /*
         * :TODO Substatement must be allowed augment target type e.g.
         * Container, etc... and must not be for example grouping, identity etc.
         * It is problem in case when more than one substatements have the same
         * QName, for example Grouping and Container are siblings and they have
         * the same QName. We must find the Container and the Grouping must be
         * ignored as disallowed augment target.
         */
        final Collection<?> allowedAugmentTargets = substatementCtx.namespaceItem(
            ValidationBundles.NAMESPACE, ValidationBundleType.SUPPORTED_AUGMENT_TARGETS);

        // if no allowed target is returned we consider all targets allowed
        return allowedAugmentTargets == null || allowedAugmentTargets.isEmpty()
                || allowedAugmentTargets.contains(substatementCtx.publicDefinition());
    }
}
