/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
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

        // 'augment' statement in a 'module' or 'submodule' is implied from this point on: determine the augment's home
        // module, as we will need it to determine the augment's relationship to the target
        final var augmentModule = augmentParent.currentModule();

        // 'augment' statement and target node are in the same module
        if (augmentModule.equals(targetNode.currentModule())) {
            return AugmentStrategy.SAME_MODULE;
        }

        // At his point we know we are introducing nodes into another module, but we also need to discern whether or not
        // we are the targeting a node introduced by another module or another augment statement with the same home
        // module.
        //
        // YANG does not allow circular module dependencies via imports so in any schema node identifier a namespace is
        // either not present in any of the steps, or present in exactly one contiguous span of steps.
        //
        // An invariant on entry to this method is that targetNode is a schema tree statement and it matches augment
        // statement's argument: therefore the last SchemaNodeIdentifier step equals target node's argument.
        final var stepsToTarget = augmentNode.getArgument().getNodeIdentifiers();
        final var lastStep = stepsToTarget.getLast();
        if (!lastStep.equals(targetNode.argument())) {
            throw new VerifyException("Mismatch between " + augmentNode.getArgument() + " and " + targetNode);
        }

        // compare target node's namespace to discern between the two cases
        return augmentModule.equals(lastStep.getModule())
            // we are adding nodes to a node introduced by a different augment statement that has the same home module
            ? strategyFor(augmentModule, augmentParent, stepsToTarget.iterator())
            // we are adding nodes to a node introduced by another module
            : statementSupport.strategyFor(augmentNode);
    }

    @NonNullByDefault
    private AugmentStrategy strategyFor(final QNameModule augmentModule, final StmtContext<?, ?, ?> augmentParent,
            final Iterator<QName> stepsToTarget) {
        // In order to determine the correct strategy here we need to examine the entire span of augment's home module
        // nodes.
        //
        // We could perform a reverse walk through targetNode's ancestor hierarchy resulting in fewer steps at
        // the cost of verifying each of them has a QName argument -- but instead choose re-lookup the statements.
        //
        // stepsToTarget is known end with a span of steps that match augmentModule, so let's rewind state so that:
        // - step is the first step in that span
        // - stmt is the corresponding statement
        var stmt = augmentParent;
        QName step;
        do {
            step = stepsToTarget.next();
            stmt = stmt.getNamespaceItem(ParserNamespaces.schemaTree(), step);
        } while (!augmentModule.equals(step.getModule()));

        // now process the span to see if we have an ancestor that implies a strategy
        while (true) {
            // mandatory enforcement point implies none of its children are mandatory (as seen by the target module)
            if (isMandatoryEnforcementRoot(stmt)) {
                return AugmentStrategy.SAME_MODULE;
            }

            if (!stepsToTarget.hasNext()) {
                break;
            }

            step = stepsToTarget.next();
            stmt = stmt.getNamespaceItem(ParserNamespaces.schemaTree(), step);
        }

        // defer to support for policy
        return statementSupport.strategyFor(augmentNode);
    }

    // down to one of three possible cases:
    // - a container with presence
    // - a choice with effective 'mandatory false'
    // - a list with effective 'min-elements 0'
    private static boolean isMandatoryEnforcementRoot(final StmtContext<?, ?, ?> stmt) {
        if (stmt.produces(ContainerStatement.DEF)) {
            return stmt.hasSubstatement(PresenceEffectiveStatement.class);
        }
        if (stmt.produces(ChoiceStatement.DEF)) {
            final var arg = StmtContextUtils.firstAttributeOf(stmt.allSubstatements(), MandatoryStatement.class);
            return arg == null || !arg;
        }
        if (stmt.produces(ListStatement.DEF)) {
            final var arg = StmtContextUtils.firstAttributeOf(stmt.allSubstatements(), MinElementsStatement.class);
            return arg == null || arg.matchesAll();
        }
        return false;
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
