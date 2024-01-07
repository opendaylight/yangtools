/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UsesStatementSupport
        extends AbstractQNameStatementSupport<UsesStatement, UsesEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementSupport.class);
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.USES)
            .addAny(YangStmtMapping.AUGMENT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.REFINE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    public UsesStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.USES, StatementPolicy.exactReplica(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, UsesStatement, UsesEffectiveStatement> usesNode) {
        super.onFullDefinitionDeclared(usesNode);

        final var usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var groupingName = usesNode.argument();

        final var sourceGroupingPre = usesAction.requiresCtx(usesNode, ParserNamespaces.GROUPING, groupingName,
                ModelProcessingPhase.EFFECTIVE_MODEL);
        final var targetNodePre = usesAction.mutatesEffectiveCtx(usesNode.getParentContext());

        usesAction.apply(new InferenceAction() {

            @Override
            public void apply(final InferenceContext ctx) {
                final var targetNodeStmtCtx = targetNodePre.resolve(ctx);
                final var sourceGrpStmtCtx = sourceGroupingPre.resolve(ctx);

                copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);

                // Apply any refine statements
                for (var subStmt : usesNode.mutableDeclaredSubstatements()) {
                    if (subStmt.producesDeclared(RefineStatement.class) && subStmt.isSupportedToBuildEffective()) {
                        performRefine(subStmt, targetNodeStmtCtx);
                    }
                }

                StmtContextUtils.validateIfFeatureAndWhenOnListKeys(usesNode);
                usesNode.addToNs(SourceGroupingNamespace.INSTANCE, Empty.value(), sourceGrpStmtCtx);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                usesNode.inferFalse(failed.contains(sourceGroupingPre),
                    "Grouping '%s' was not resolved.", groupingName);
                throw usesNode.newInferenceException("Unknown error occurred.");
            }
        });
    }

    @Override
    protected UsesStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createUses(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected UsesStatement attachDeclarationReference(final UsesStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateUses(stmt, reference);
    }

    @Override
    protected UsesEffectiveStatement createEffective(final Current<QName, UsesStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final EffectiveStatement<?, ?> source =
            verifyNotNull(stmt.namespaceItem(SourceGroupingNamespace.INSTANCE, Empty.value())).buildEffective();
        verify(source instanceof GroupingDefinition, "Unexpected source %s", source);
        final GroupingDefinition sourceGrouping = (GroupingDefinition) source;

        final int flags = EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), substatements);
        final QName argument = stmt.getArgument();
        final UsesStatement declared = stmt.declared();

        if (substatements.isEmpty()) {
            return argument.equals(declared.argument())
                ? new EmptyLocalUsesEffectiveStatement(declared, sourceGrouping, flags)
                        : new SimpleCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags);
        }

        if (declared.argument().equals(argument)) {
            return new RegularLocalUsesEffectiveStatement(declared, sourceGrouping, flags, substatements);
        }
        if (findFirstStatement(substatements, RefineEffectiveStatement.class) == null) {
            return new SimpleCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags, substatements);
        }
        return new FullCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags, substatements);
    }

    /**
     * Copy statements from a grouping to a target node.
     *
     * @param sourceGrpStmtCtx
     *            source grouping statement context
     * @param targetCtx
     *            target context
     * @param usesNode
     *            uses node
     * @throws SourceException
     *             instance of SourceException
     */
    private static void copyFromSourceToTarget(final StmtContext<?, ?, ?> sourceGrpStmtCtx,
            final Mutable<?, ?, ?> targetCtx, final Mutable<QName, UsesStatement, UsesEffectiveStatement> usesNode) {
        final var declared = sourceGrpStmtCtx.declaredSubstatements();
        final var effective = sourceGrpStmtCtx.effectiveSubstatements();
        final var buffer = new ArrayList<Mutable<?, ?, ?>>(declared.size() + effective.size());
        final var newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);
        final var unsupported = !usesNode.isSupportedByFeatures();

        for (var original : declared) {
            if (shouldCopy(original)) {
                original.copyAsChildOf(targetCtx, CopyType.ADDED_BY_USES, newQNameModule).ifPresent(copy -> {
                    if (unsupported || !original.isSupportedByFeatures() || !original.isSupportedToBuildEffective()) {
                        copy.setUnsupported();
                    }
                    buffer.add(copy);
                });
            }
        }

        for (var original : effective) {
            if (shouldCopy(original)) {
                original.copyAsChildOf(targetCtx, CopyType.ADDED_BY_USES, newQNameModule).ifPresent(buffer::add);
            }
        }

        targetCtx.addEffectiveSubstatements(buffer);
        usesNode.addAsEffectOfStatement(buffer);
    }

    private static boolean shouldCopy(final StmtContext<?, ?, ?> stmt) {
        // https://www.rfc-editor.org/rfc/rfc7950#section-7.13:
        //
        //        The effect of a "uses" reference to a grouping is that the nodes
        //        defined by the grouping are copied into the current schema tree and
        //        are then updated according to the "refine" and "augment" statements.
        //
        // This means that the statement that is about to be copied (and can be subjected to buildEffective() I think)
        // is actually a SchemaTreeEffectiveStatement
        if (SchemaTreeEffectiveStatement.class.isAssignableFrom(
                stmt.publicDefinition().getEffectiveRepresentationClass())) {
            return true;
        }

        // As per https://www.rfc-editor.org/rfc/rfc7950#section-7.13.2:
        //
        //        o  Any node can get refined extensions, if the extension allows
        //           refinement.  See Section 7.19 for details.
        //
        // and https://www.rfc-editor.org/rfc/rfc7950#section-7.19:
        //
        //        An extension can allow refinement (see Section 7.13.2) and deviations
        //        (Section 7.20.3.2), but the mechanism for how this is defined is
        //        outside the scope of this specification.
        //
        // This is actively used out there (tailf-common.yang's tailf:action), which is incorrect, though. They do
        // publish a bunch of metadata (through tailf-meta-extension.yang), but fail to publish a key aspect of the
        // statement: it attaches to schema tree namespace (just as RFC7950 action does). Such an extension would
        // automatically result in the extension being picked up by the above check and everybody would live happily
        // ever after.
        //
        // We do not live in that world yet, hence we do the following and keep our fingers crossed.
        // FIXME: YANGTOOLS-403: this should not be necessary once we implement the above (although tests will complain)
        return StmtContextUtils.isUnknownStatement(stmt);
    }

    private static QNameModule getNewQNameModule(final StmtContext<?, ?, ?> targetCtx,
            final StmtContext<?, ?, ?> stmtContext) {
        if (targetCtx.getParentContext() == null) {
            return targetCtx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, targetCtx);
        }
        if (targetCtx.publicDefinition() == YangStmtMapping.AUGMENT) {
            return StmtContextUtils.getModuleQName(targetCtx);
        }
        if (targetCtx.argument() instanceof QName targetQName && stmtContext.argument() instanceof QName) {
            return targetQName.getModule();
        }

        return null;
    }

    private static void performRefine(final Mutable<?, ?, ?> refineStmtCtx, final StmtContext<?, ?, ?> usesParentCtx) {
        final Object refineArgument = refineStmtCtx.argument();
        if (!(refineArgument instanceof Descendant refineDescendant)) {
            throw refineStmtCtx.newInferenceException(
                "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier.", refineArgument);
        }

        // FIXME: this really should be handled via separate inference, i.e. we first instantiate the template and when
        //        it appears, this refine will trigger on it. This reinforces the FIXME below.
        final var optRefineTargetCtx = ParserNamespaces.findSchemaTreeStatement(usesParentCtx, refineDescendant);
        refineStmtCtx.inferTrue(optRefineTargetCtx.isPresent(), "Refine target node %s not found.", refineDescendant);

        // FIXME: This communicates the looked-up target node to RefineStatementSupport.buildEffective(). We should do
        //        this trick through a shared namespace or similar reactor-agnostic meeting place. It really feels like
        //        an inference action RefineStatementSupport should be doing.
        final var refineTargetCtx = optRefineTargetCtx.orElseThrow();
        if (StmtContextUtils.isUnknownStatement(refineTargetCtx)) {
            LOG.trace("Refine node '{}' in uses '{}' has target node unknown statement '{}'. "
                + "Refine has been skipped. At line: {}", refineStmtCtx.argument(),
                refineStmtCtx.coerceParentContext().argument(), refineTargetCtx.argument(),
                refineStmtCtx.sourceReference());
        } else if (refineTargetCtx instanceof Mutable<?, ?, ?> refineTarget) {
            for (var refineSubstatementCtx : refineStmtCtx.declaredSubstatements()) {
                if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                    addOrReplaceNode(refineSubstatementCtx, refineTarget);
                }
            }
        } else {
            throw new VerifyException("Unexpected target " + refineTargetCtx);
        }

        // Target is a prerequisite for the 'refine', hence if the target is not supported, the refine is not supported
        // as well.
        if (!refineTargetCtx.isSupportedToBuildEffective() || !refineTargetCtx.isSupportedByFeatures()) {
            refineStmtCtx.setUnsupported();
        }
    }

    private static void addOrReplaceNode(final StmtContext<?, ?, ?> refineSubstatementCtx,
            final Mutable<?, ?, ?> refineTargetNodeCtx) {

        final StatementDefinition refineSubstatementDef = refineSubstatementCtx.publicDefinition();

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
        final Collection<?> supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS.get(
            refineSubstatementCtx.publicDefinition());

        return supportedRefineTargets == null || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx.publicDefinition());
    }
}
