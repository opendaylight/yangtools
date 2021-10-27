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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.common.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.refine.RefineTargetNamespace;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.SchemaTreeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
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
        if (!usesNode.isSupportedByFeatures()) {
            return;
        }
        super.onFullDefinitionDeclared(usesNode);

        final ModelActionBuilder usesAction = usesNode.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final QName groupingName = usesNode.argument();

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
                usesNode.addToNs(SourceGroupingNamespace.class, Empty.getInstance(), sourceGrpStmtCtx);
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                InferenceException.throwIf(failed.contains(sourceGroupingPre), usesNode,
                    "Grouping '%s' was not resolved.", groupingName);
                throw new InferenceException("Unknown error occurred.", usesNode);
            }
        });
    }

    @Override
    protected UsesStatement createDeclared(final StmtContext<QName, UsesStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
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
            verifyNotNull(stmt.getFromNamespace(SourceGroupingNamespace.class, Empty.getInstance())).buildEffective();
        verify(source instanceof GroupingDefinition, "Unexpected source %s", source);
        final GroupingDefinition sourceGrouping = (GroupingDefinition) source;

        final int flags = EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements);
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
                stmt.publicDefinition().getEffectiveRepresentationClass())) {
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
        if (targetCtx.publicDefinition() == YangStmtMapping.AUGMENT) {
            return StmtContextUtils.getRootModuleQName(targetCtx);
        }

        final Object targetStmtArgument = targetCtx.argument();
        final Object sourceStmtArgument = stmtContext.argument();
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
        return !YangVersion.VERSION_1_1.equals(subStmtCtx.yangVersion()) || subStmtCtx.isSupportedByFeatures();
    }

    private static void performRefine(final Mutable<?, ?, ?> subStmtCtx, final StmtContext<?, ?, ?> usesParentCtx) {
        final Object refineArgument = subStmtCtx.argument();
        InferenceException.throwIf(!(refineArgument instanceof SchemaNodeIdentifier), subStmtCtx,
            "Invalid refine argument %s. It must be instance of SchemaNodeIdentifier.", refineArgument);

        final Optional<StmtContext<?, ?, ?>> optRefineTargetCtx = SchemaTreeNamespace.findNode(
            usesParentCtx, (SchemaNodeIdentifier) refineArgument);
        InferenceException.throwIf(!optRefineTargetCtx.isPresent(), subStmtCtx, "Refine target node %s not found.",
            refineArgument);

        // FIXME: This communicates the looked-up target node to RefineStatementSupport.buildEffective(). We should do
        //        this trick through a shared namespace or similar reactor-agnostic meeting place. It really feels like
        //        an inference action RefineStatementSupport should be doing.
        final StmtContext<?, ?, ?> refineTargetNodeCtx = optRefineTargetCtx.get();
        if (StmtContextUtils.isUnknownStatement(refineTargetNodeCtx)) {
            LOG.trace("Refine node '{}' in uses '{}' has target node unknown statement '{}'. "
                + "Refine has been skipped. At line: {}", subStmtCtx.argument(),
                subStmtCtx.coerceParentContext().argument(), refineTargetNodeCtx.argument(),
                subStmtCtx.sourceReference());
        } else {
            verify(refineTargetNodeCtx instanceof StatementContextBase);
            addOrReplaceNodes(subStmtCtx, (StatementContextBase<?, ?, ?>) refineTargetNodeCtx);
        }

        // Target is a prerequisite for the 'refine', hence if the target is not supported, the refine is not supported
        // as well. Otherwise add a pointer to the target into refine's local namespace.
        if (refineTargetNodeCtx.isSupportedToBuildEffective()) {
            subStmtCtx.addToNs(RefineTargetNamespace.class, Empty.getInstance(), refineTargetNodeCtx);
        } else {
            subStmtCtx.setIsSupportedToBuildEffective(false);
        }
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

        final StatementDefinition refineSubstatementDef = refineSubstatementCtx.publicDefinition();

        SourceException.throwIf(!isSupportedRefineTarget(refineSubstatementCtx, refineTargetNodeCtx),
                refineSubstatementCtx,
                "Error in module '%s' in the refine of uses '%s': can not perform refine of '%s' for the target '%s'.",
                refineSubstatementCtx.getRoot().rawArgument(), refineSubstatementCtx.coerceParentContext().argument(),
                refineSubstatementCtx.publicDefinition(), refineTargetNodeCtx.publicDefinition());

        if (!isAllowedToAddByRefine(refineSubstatementDef)) {
            refineTargetNodeCtx.removeStatementFromEffectiveSubstatements(refineSubstatementDef);
        }
        refineTargetNodeCtx.addEffectiveSubstatement(refineSubstatementCtx.replicaAsChildOf(refineTargetNodeCtx));
    }

    // FIXME: clarify this and inline into single caller
    private static boolean isAllowedToAddByRefine(final StatementDefinition publicDefinition) {
        return YangStmtMapping.MUST.equals(publicDefinition);
    }

    private static boolean isSupportedRefineSubstatement(final StmtContext<?, ?, ?> refineSubstatementCtx) {
        final Collection<?> supportedRefineSubstatements = refineSubstatementCtx.getFromNamespace(
                ValidationBundlesNamespace.class, ValidationBundleType.SUPPORTED_REFINE_SUBSTATEMENTS);

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
