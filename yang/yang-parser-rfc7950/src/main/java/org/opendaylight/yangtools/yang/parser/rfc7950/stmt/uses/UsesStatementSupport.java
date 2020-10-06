/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.uses;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UsesStatementSupport
        extends BaseQNameStatementSupport<UsesStatement, UsesEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(UsesStatementSupport.class);
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .USES)
        .addAny(YangStmtMapping.AUGMENT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addAny(YangStmtMapping.REFINE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final UsesStatementSupport INSTANCE = new UsesStatementSupport();

    private UsesStatementSupport() {
        super(YangStmtMapping.USES);
    }

    public static UsesStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseNodeIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, UsesStatement, UsesEffectiveStatement> usesNode) {
        if (!usesNode.isSupportedByFeatures()) {
            return;
        }
        super.onFullDefinitionDeclared(usesNode);

        final ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final QName groupingName = usesNode.getStatementArgument();

        final Prerequisite<StmtContext<?, ?, ?>> sourceGroupingPre = usesAction.requiresCtx(usesNode,
                GroupingNamespace.class, groupingName, ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<? extends StmtContext.Mutable<?, ?, ?>> targetNodePre = usesAction.mutatesEffectiveCtx(
                usesNode.getParentContext());

        usesAction.apply(new InferenceAction() {

            @Override
            public void apply(final InferenceContext ctx) {
                final StatementContextBase<?, ?, ?> targetNodeStmtCtx =
                        (StatementContextBase<?, ?, ?>) targetNodePre.resolve(ctx);
                final StatementContextBase<?, ?, ?> sourceGrpStmtCtx =
                        (StatementContextBase<?, ?, ?>) sourceGroupingPre.resolve(ctx);

                copyFromSourceToTarget(sourceGrpStmtCtx, targetNodeStmtCtx, usesNode);
                resolveUsesNode(usesNode, targetNodeStmtCtx);
                StmtContextUtils.validateIfFeatureAndWhenOnListKeys(usesNode);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(sourceGroupingPre),
                        usesNode.getStatementSourceReference(), "Grouping '%s' was not resolved.", groupingName);
                throw new InferenceException("Unknown error occurred.", usesNode.getStatementSourceReference());
            }
        });
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected UsesStatement createDeclared(final StmtContext<QName, UsesStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularUsesStatement(ctx, substatements);
    }

    @Override
    protected UsesStatement createEmptyDeclared(final StmtContext<QName, UsesStatement, ?> ctx) {
        return new EmptyUsesStatement(ctx);
    }

    @Override
    protected UsesEffectiveStatement createEffective(
            final StmtContext<QName, UsesStatement, UsesEffectiveStatement> ctx, final UsesStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final GroupingDefinition sourceGrouping = getSourceGrouping(ctx);
        final int flags = historyAndStatusFlags(ctx, substatements);
        final QName argument = ctx.coerceStatementArgument();
        if (declared.argument().equals(argument)) {
            return new RegularLocalUsesEffectiveStatement(declared, sourceGrouping, flags, substatements);
        }
        if (findFirstStatement(substatements, RefineEffectiveStatement.class) == null) {
            return new SimpleCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags, substatements);
        }
        return new FullCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags, substatements);
    }

    @Override
    protected UsesEffectiveStatement createEmptyEffective(
            final StmtContext<QName, UsesStatement, UsesEffectiveStatement> ctx, final UsesStatement declared) {
        final GroupingDefinition sourceGrouping = getSourceGrouping(ctx);
        final int flags = historyAndStatusFlags(ctx, ImmutableList.of());
        final QName argument = ctx.coerceStatementArgument();
        return argument.equals(declared.argument())
                ? new EmptyLocalUsesEffectiveStatement(declared, sourceGrouping, flags)
                        : new SimpleCopiedUsesEffectiveStatement(declared, argument, sourceGrouping, flags);
    }

    static @NonNull ImmutableMap<Descendant, SchemaNode> indexRefines(
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final Map<Descendant, SchemaNode> refines = new LinkedHashMap<>();

        for (EffectiveStatement<?, ?> effectiveStatement : substatements) {
            if (effectiveStatement instanceof RefineEffectiveStatementImpl) {
                final RefineEffectiveStatementImpl refineStmt = (RefineEffectiveStatementImpl) effectiveStatement;
                refines.put(refineStmt.argument(), refineStmt.getRefineTargetNode());
            }
        }

        return ImmutableMap.copyOf(refines);
    }

    private static GroupingDefinition getSourceGrouping(final StmtContext<QName, ?, ?> ctx) {
        return (GroupingDefinition) ctx.getFromNamespace(GroupingNamespace.class, ctx.coerceStatementArgument())
                .buildEffective();
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
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static void copyFromSourceToTarget(final Mutable<?, ?, ?> sourceGrpStmtCtx,
            final StatementContextBase<?, ?, ?> targetCtx,
            final Mutable<QName, UsesStatement, UsesEffectiveStatement> usesNode) {
        final Collection<? extends Mutable<?, ?, ?>> declared = sourceGrpStmtCtx.mutableDeclaredSubstatements();
        final Collection<? extends Mutable<?, ?, ?>> effective = sourceGrpStmtCtx.mutableEffectiveSubstatements();
        final Collection<Mutable<?, ?, ?>> buffer = new ArrayList<>(declared.size() + effective.size());
        final QNameModule newQNameModule = getNewQNameModule(targetCtx, sourceGrpStmtCtx);

        for (final Mutable<?, ?, ?> original : declared) {
            if (original.isSupportedByFeatures() && shouldCopy(original)) {
                original.copyAsChildOf(targetCtx, CopyType.ADDED_BY_USES, newQNameModule).ifPresent(buffer::add);
            }
        }

        for (final Mutable<?, ?, ?> original : effective) {
            if (shouldCopy(original)) {
                original.copyAsChildOf(targetCtx, CopyType.ADDED_BY_USES, newQNameModule).ifPresent(buffer::add);
            }
        }

        targetCtx.addEffectiveSubstatements(buffer);
        usesNode.addAsEffectOfStatement(buffer);
    }

    private static boolean shouldCopy(final StmtContext<?, ?, ?> stmt) {
        // https://tools.ietf.org/html/rfc7950#section-7.13:
        //
        //        The effect of a "uses" reference to a grouping is that the nodes
        //        defined by the grouping are copied into the current schema tree and
        //        are then updated according to the "refine" and "augment" statements.
        //
        // This means that the statement that is about to be copied (and can be subjected to buildEffective() I think)
        // is actually a SchemaTreeEffectiveStatement
        if (SchemaTreeEffectiveStatement.class.isAssignableFrom(
                stmt.getPublicDefinition().getEffectiveRepresentationClass())) {
            return true;
        }

        // As per https://tools.ietf.org/html/rfc7950#section-7.13.2:
        //
        //        o  Any node can get refined extensions, if the extension allows
        //           refinement.  See Section 7.19 for details.
        //
        // and https://tools.ietf.org/html/rfc7950#section-7.19:
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
            return targetCtx.getFromNamespace(ModuleCtxToModuleQName.class, targetCtx);
        }
        if (targetCtx.getPublicDefinition() == YangStmtMapping.AUGMENT) {
            return StmtContextUtils.getRootModuleQName(targetCtx);
        }

        final Object targetStmtArgument = targetCtx.getStatementArgument();
        final Object sourceStmtArgument = stmtContext.getStatementArgument();
        if (targetStmtArgument instanceof QName && sourceStmtArgument instanceof QName) {
            return ((QName) targetStmtArgument).getModule();
        }

        return null;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static void resolveUsesNode(final Mutable<QName, UsesStatement, UsesEffectiveStatement> usesNode,
            final StmtContext<?, ?, ?> targetNodeStmtCtx) {
        for (final Mutable<?, ?, ?> subStmtCtx : usesNode.mutableDeclaredSubstatements()) {
            if (subStmtCtx.producesDeclared(RefineStatement.class) && areFeaturesSupported(subStmtCtx)) {
                performRefine(subStmtCtx, targetNodeStmtCtx);
            }
        }
    }

    private static boolean areFeaturesSupported(final StmtContext<?, ?, ?> subStmtCtx) {
        /*
         * In case of Yang 1.1, checks whether features are supported.
         */
        return !YangVersion.VERSION_1_1.equals(subStmtCtx.getRootVersion()) || subStmtCtx.isSupportedByFeatures();
    }

    private static void performRefine(final Mutable<?, ?, ?> subStmtCtx, final StmtContext<?, ?, ?> usesParentCtx) {
        final Object refineArgument = subStmtCtx.getStatementArgument();
        InferenceException.throwIf(!(refineArgument instanceof SchemaNodeIdentifier),
            subStmtCtx.getStatementSourceReference(),
            "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier.", refineArgument);

        final Optional<StmtContext<?, ?, ?>> optRefineTargetCtx = ChildSchemaNodeNamespace.findNode(
            usesParentCtx, (SchemaNodeIdentifier) refineArgument);
        InferenceException.throwIf(!optRefineTargetCtx.isPresent(), subStmtCtx.getStatementSourceReference(),
            "Refine target node %s not found.", refineArgument);

        final StmtContext<?, ?, ?> refineTargetNodeCtx = optRefineTargetCtx.get();
        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.trace("Refine node '{}' in uses '{}' has target node unknown statement '{}'. "
                + "Refine has been skipped. At line: {}", subStmtCtx.getStatementArgument(),
                subStmtCtx.coerceParentContext().getStatementArgument(),
                refineTargetNodeCtx.getStatementArgument(), subStmtCtx.getStatementSourceReference());
            subStmtCtx.addAsEffectOfStatement(refineTargetNodeCtx);
            return;
        }

        Verify.verify(refineTargetNodeCtx instanceof StatementContextBase);
        addOrReplaceNodes(subStmtCtx, (StatementContextBase<?, ?, ?>) refineTargetNodeCtx);
        subStmtCtx.addAsEffectOfStatement(refineTargetNodeCtx);
    }

    private static void addOrReplaceNodes(final Mutable<?, ?, ?> subStmtCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {
        for (final Mutable<?, ?, ?> refineSubstatementCtx : subStmtCtx.mutableDeclaredSubstatements()) {
            if (isSupportedRefineSubstatement(refineSubstatementCtx)) {
                addOrReplaceNode(refineSubstatementCtx, refineTargetNodeCtx);
            }
        }
    }

    private static void addOrReplaceNode(final Mutable<?, ?, ?> refineSubstatementCtx,
            final StatementContextBase<?, ?, ?> refineTargetNodeCtx) {

        final StatementDefinition refineSubstatementDef = refineSubstatementCtx.getPublicDefinition();

        SourceException.throwIf(!isSupportedRefineTarget(refineSubstatementCtx, refineTargetNodeCtx),
                refineSubstatementCtx.getStatementSourceReference(),
                "Error in module '%s' in the refine of uses '%s': can not perform refine of '%s' for the target '%s'.",
                refineSubstatementCtx.getRoot().rawStatementArgument(),
                refineSubstatementCtx.coerceParentContext().getStatementArgument(),
                refineSubstatementCtx.getPublicDefinition(), refineTargetNodeCtx.getPublicDefinition());

        if (isAllowedToAddByRefine(refineSubstatementDef)) {
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        } else {
            refineTargetNodeCtx.removeStatementFromEffectiveSubstatements(refineSubstatementDef);
            refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx);
        }
    }

    private static boolean isAllowedToAddByRefine(final StatementDefinition publicDefinition) {
        return YangStmtMapping.MUST.equals(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(final StmtContext<?, ?, ?> refineSubstatementCtx) {
        final Collection<?> supportedRefineSubstatements = refineSubstatementCtx.getFromNamespace(
                ValidationBundlesNamespace.class, ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

        return supportedRefineSubstatements == null || supportedRefineSubstatements.isEmpty()
                || supportedRefineSubstatements.contains(refineSubstatementCtx.getPublicDefinition())
                || StmtContextUtils.isUnknownStatement(refineSubstatementCtx);
    }

    private static boolean isSupportedRefineTarget(final StmtContext<?, ?, ?> refineSubstatementCtx,
            final StmtContext<?, ?, ?> refineTargetNodeCtx) {
        final Collection<?> supportedRefineTargets = YangValidationBundles.SUPPORTED_REFINE_TARGETS.get(
            refineSubstatementCtx.getPublicDefinition());

        return supportedRefineTargets == null || supportedRefineTargets.isEmpty()
                || supportedRefineTargets.contains(refineTargetNodeCtx.getPublicDefinition());
    }
}
