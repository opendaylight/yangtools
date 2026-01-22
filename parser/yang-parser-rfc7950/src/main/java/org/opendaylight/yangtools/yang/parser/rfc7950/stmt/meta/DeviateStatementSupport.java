/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.YangValidationBundles;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeviateStatementSupport
        extends AbstractStatementSupport<DeviateArgument, DeviateStatement, DeviateEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(DeviateStatementSupport.class);

    // Shared by both
    private static final SubstatementValidator NOT_SUPPORTED_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION).build();
    private static final SubstatementValidator REPLACE_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addOptional(YangStmtMapping.TYPE)
            .addOptional(YangStmtMapping.UNITS)
            .build();

    // RFC6020
    private static final SubstatementValidator RFC6020_ADD_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.UNIQUE)
            .addOptional(YangStmtMapping.UNITS)
            .build();
    private static final SubstatementValidator RFC6020_DELETE_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION)
            .addOptional(YangStmtMapping.DEFAULT)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.UNIQUE)
            .addOptional(YangStmtMapping.UNITS)
            .build();

    // RFC7950
    private static final SubstatementValidator RFC7950_ADD_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.MANDATORY)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.UNIQUE)
            .addOptional(YangStmtMapping.UNITS)
            .build();
    private static final SubstatementValidator RFC7950_DELETE_VALIDATOR =
        SubstatementValidator.builder(DeviateStatement.DEFINITION)
            .addAny(YangStmtMapping.DEFAULT)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.UNIQUE)
            .addOptional(YangStmtMapping.UNITS)
            .build();

    private static final Set<YangStmtMapping> IMPLICIT_STATEMENTS = Set.of(
        YangStmtMapping.CONFIG,
        YangStmtMapping.MANDATORY,
        YangStmtMapping.MAX_ELEMENTS,
        YangStmtMapping.MIN_ELEMENTS);
    private static final Set<YangStmtMapping> SINGLETON_STATEMENTS = Set.of(
        YangStmtMapping.CONFIG,
        YangStmtMapping.MANDATORY,
        YangStmtMapping.MIN_ELEMENTS,
        YangStmtMapping.MAX_ELEMENTS,
        YangStmtMapping.UNITS);

    private final SubstatementValidator addValidator;
    private final SubstatementValidator deleteValidator;

    private DeviateStatementSupport(final YangParserConfiguration config,
            final SubstatementValidator addValidator, final SubstatementValidator deleteValidator) {
        // Note: we are performing our own validation based on deviate kind.
        // TODO: perhaps we should do argumentSpecificSupport?
        super(DeviateStatement.DEFINITION, StatementPolicy.contextIndependent(), SubtreePolicy.template(), config,
            null);
        this.addValidator = requireNonNull(addValidator);
        this.deleteValidator = requireNonNull(deleteValidator);
    }

    public static @NonNull DeviateStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new DeviateStatementSupport(config, RFC6020_ADD_VALIDATOR, RFC6020_DELETE_VALIDATOR);
    }

    public static @NonNull DeviateStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new DeviateStatementSupport(config, RFC7950_ADD_VALIDATOR, RFC7950_DELETE_VALIDATOR);
    }

    @Override
    public DeviateArgument parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SourceException.throwIfNull(DeviateArgument.forArgument(value), ctx,
            "String '%s' is not valid deviate argument", value);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<DeviateArgument, DeviateStatement, DeviateEffectiveStatement> deviateCtx) {
        final var deviateArg = deviateCtx.argument();
        getSubstatementValidatorForDeviate(deviateArg).validate(deviateCtx);

        final var parent = deviateCtx.coerceParentContext();
        final var parentDeviation = parent.tryDeclaring(DeviationStatement.class);
        if (parentDeviation == null) {
            throw new InferenceException(deviateCtx, "Unexpected parent statement %s",
                parent.publicDefinition().humanName());
        }

        final var targetNode = parentDeviation.getArgument();
        if (!isDeviationSupported(deviateCtx, targetNode)) {
            return;
        }

        final var deviateAction = deviateCtx.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final var sourceCtxPrerequisite = deviateAction.requiresCtx(deviateCtx, ModelProcessingPhase.EFFECTIVE_MODEL);
        final var targetCtxPrerequisite = deviateAction.mutatesEffectiveCtxPath(deviateCtx.getRoot(),
            ParserNamespaces.schemaTree(), targetNode.getNodeIdentifiers());

        deviateAction.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                if (!deviateCtx.isSupportedToBuildEffective()) {
                    // We are not building effective model, hence we should not be performing any effects
                    return;
                }

                final var sourceNodeStmtCtx = sourceCtxPrerequisite.resolve(ctx);
                final var targetNodeStmtCtx = targetCtxPrerequisite.resolve(ctx);

                switch (deviateArg) {
                    case null -> throw new NullPointerException();
                    case NOT_SUPPORTED ->
                        // FIXME: this can be short-circuited without an inference action
                        targetNodeStmtCtx.setUnsupported();
                    case ADD -> performDeviateAdd(sourceNodeStmtCtx, targetNodeStmtCtx);
                    case REPLACE -> performDeviateReplace(sourceNodeStmtCtx, targetNodeStmtCtx);
                    case DELETE -> performDeviateDelete(sourceNodeStmtCtx, targetNodeStmtCtx);
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                // FIXME: improve formatting
                throw new InferenceException(deviateCtx.coerceParentContext(), "Deviation target '%s' not found.",
                    targetNode);
            }

            @Override
            public void prerequisiteUnavailable(final Prerequisite<?> unavail) {
                if (targetCtxPrerequisite.equals(unavail)) {
                    deviateCtx.setUnsupported();
                } else {
                    prerequisiteFailed(List.of(unavail));
                }
            }
        });
    }

    @Override
    public String internArgument(final String rawArgument) {
        return switch (rawArgument) {
            case "add" -> "add";
            case "delete" -> "delete";
            case "replace" -> "replace";
            case "not-supported" -> "not-supported";
            case null, default -> rawArgument;
        };
    }

    @Override
    protected DeviateStatement createDeclared(final BoundStmtCtx<DeviateArgument> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createDeviate(ctx.getArgument(), substatements);
    }

    @Override
    protected DeviateStatement attachDeclarationReference(final DeviateStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateDeviate(stmt, reference);
    }

    @Override
    protected DeviateEffectiveStatement createEffective(final Current<DeviateArgument, DeviateStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createDeviate(stmt.declared(), substatements);
    }

    protected SubstatementValidator getSubstatementValidatorForDeviate(final DeviateArgument deviateKind) {
        return switch (deviateKind) {
            case ADD -> addValidator;
            case DELETE -> deleteValidator;
            case NOT_SUPPORTED -> NOT_SUPPORTED_VALIDATOR;
            case REPLACE -> REPLACE_VALIDATOR;
        };
    }

    private static boolean isDeviationSupported(
            final Mutable<DeviateArgument, DeviateStatement, DeviateEffectiveStatement> deviateStmtCtx,
            final SchemaNodeIdentifier deviationTarget) {
        final var modulesDeviatedByModules = deviateStmtCtx.namespaceItem(ParserNamespaces.MODULES_DEVIATED_BY,
            Empty.value());
        if (modulesDeviatedByModules == null) {
            return true;
        }

        final var currentModule = deviateStmtCtx.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME,
                deviateStmtCtx.getRoot());
        final var targetModule = deviationTarget.getNodeIdentifiers().getLast().getModule();
        final var deviationModulesSupportedByTargetModule = modulesDeviatedByModules.get(targetModule);
        if (deviationModulesSupportedByTargetModule != null) {
            return deviationModulesSupportedByTargetModule.contains(currentModule);
        }

        return false;
    }

    private static void performDeviateAdd(final StmtContext<?, ?, ?> deviateStmtCtx,
            final Mutable<?, ?, ?> targetCtx) {
        for (var originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
            validateDeviationTarget(originalStmtCtx, targetCtx);
            addStatement(originalStmtCtx, targetCtx);
        }
    }

    private static void addStatement(final StmtContext<?, ?, ?> stmtCtxToBeAdded, final Mutable<?, ?, ?> targetCtx) {
        if (!StmtContextUtils.isUnknownStatement(stmtCtxToBeAdded)) {
            final var stmtToBeAdded = stmtCtxToBeAdded.publicDefinition();
            if (SINGLETON_STATEMENTS.contains(stmtToBeAdded) || YangStmtMapping.DEFAULT.equals(stmtToBeAdded)
                    && LeafStatement.DEFINITION.equals(targetCtx.publicDefinition())) {
                for (var targetCtxSubstatement : targetCtx.allSubstatements()) {
                    InferenceException.throwIf(stmtToBeAdded.equals(targetCtxSubstatement.publicDefinition()),
                        stmtCtxToBeAdded, """
                            Deviation cannot add substatement %s to target node %s because it is already defined in \
                            target and can appear only once.""", stmtToBeAdded.statementName(), targetCtx.argument());
                }
            }
        }

        copyStatement(stmtCtxToBeAdded, targetCtx);
    }

    private static void performDeviateReplace(final StmtContext<?, ?, ?> deviateStmtCtx,
            final Mutable<?, ?, ?> targetCtx) {
        for (var originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
            validateDeviationTarget(originalStmtCtx, targetCtx);
            replaceStatement(originalStmtCtx, targetCtx);
        }
    }

    private static void replaceStatement(final StmtContext<?, ?, ?> stmtCtxToBeReplaced,
            final Mutable<?, ?, ?> targetCtx) {
        final var stmtToBeReplaced = stmtCtxToBeReplaced.publicDefinition();

        if (YangStmtMapping.DEFAULT.equals(stmtToBeReplaced)
                && LeafListStatement.DEFINITION.equals(targetCtx.publicDefinition())) {
            LOG.error("""
                Deviation cannot replace substatement {} in target leaf-list {} because a leaf-list can have multiple \
                default statements. At line: {}""", stmtToBeReplaced.statementName(), targetCtx.argument(),
                stmtCtxToBeReplaced.sourceReference());
            return;
        }

        for (var targetCtxSubstatement : targetCtx.effectiveSubstatements()) {
            if (stmtToBeReplaced.equals(targetCtxSubstatement.publicDefinition())) {
                targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeReplaced);
                copyStatement(stmtCtxToBeReplaced, targetCtx);
                return;
            }
        }

        for (var targetCtxSubstatement : targetCtx.mutableDeclaredSubstatements()) {
            if (stmtToBeReplaced.equals(targetCtxSubstatement.publicDefinition())) {
                targetCtxSubstatement.setUnsupported();
                copyStatement(stmtCtxToBeReplaced, targetCtx);
                return;
            }
        }

        // This is a special case when deviate replace of a config/mandatory/max/min-elements substatement targets
        // a node which does not contain an explicitly declared config/mandatory/max/min-elements.
        // However, according to RFC6020/RFC7950, these properties are always implicitly present.
        if (IMPLICIT_STATEMENTS.contains(stmtToBeReplaced)) {
            addStatement(stmtCtxToBeReplaced, targetCtx);
            return;
        }

        throw new InferenceException(stmtCtxToBeReplaced,
            "Deviation cannot replace substatement %s in target node %s because it does not exist in target node.",
            stmtToBeReplaced.statementName(), targetCtx.argument());
    }

    private static void performDeviateDelete(final StmtContext<?, ?, ?> deviateStmtCtx,
            final Mutable<?, ?, ?> targetCtx) {
        for (var originalStmtCtx : deviateStmtCtx.declaredSubstatements()) {
            validateDeviationTarget(originalStmtCtx, targetCtx);
            deleteStatement(originalStmtCtx, targetCtx);
        }
    }

    private static void deleteStatement(final StmtContext<?, ?, ?> stmtCtxToBeDeleted,
            final Mutable<?, ?, ?> targetCtx) {
        final var stmtToBeDeleted = stmtCtxToBeDeleted.publicDefinition();
        final var stmtArgument = stmtCtxToBeDeleted.rawArgument();

        for (var targetCtxSubstatement : targetCtx.mutableEffectiveSubstatements()) {
            if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.publicDefinition(),
                    targetCtxSubstatement.rawArgument())) {
                targetCtx.removeStatementFromEffectiveSubstatements(stmtToBeDeleted, stmtArgument);
                return;
            }
        }

        for (var targetCtxSubstatement : targetCtx.mutableDeclaredSubstatements()) {
            if (statementsAreEqual(stmtToBeDeleted, stmtArgument, targetCtxSubstatement.publicDefinition(),
                    targetCtxSubstatement.rawArgument())) {
                targetCtxSubstatement.setUnsupported();
                return;
            }
        }

        LOG.error("""
            Deviation cannot delete substatement {} with argument '{}' in target node {} because it does not exist in \
            the target node. At line: {}""", stmtToBeDeleted.statementName(), stmtArgument, targetCtx.argument(),
            stmtCtxToBeDeleted.sourceReference());
    }

    private static void copyStatement(final StmtContext<?, ?, ?> stmtCtxToBeCopied, final Mutable<?, ?, ?> targetCtx) {
        // we need to make a copy of the statement context only if it is an unknown statement, otherwise
        // we can reuse the original statement context
        if (!StmtContextUtils.isUnknownStatement(stmtCtxToBeCopied)) {
            // FIXME: I think this should be handled by the corresponding support's copy policy
            targetCtx.addEffectiveSubstatement(stmtCtxToBeCopied.replicaAsChildOf(targetCtx));
        } else {
            targetCtx.addEffectiveSubstatement(targetCtx.childCopyOf(stmtCtxToBeCopied, CopyType.ORIGINAL));
        }
    }

    private static boolean statementsAreEqual(final StatementDefinition firstStmtDef, final String firstStmtArg,
            final StatementDefinition secondStmtDef, final String secondStmtArg) {
        return firstStmtDef.equals(secondStmtDef) && Objects.equals(firstStmtArg, secondStmtArg);
    }

    private static void validateDeviationTarget(final StmtContext<?, ?, ?> deviateSubStmtCtx,
            final StmtContext<?, ?, ?> targetCtx) {
        InferenceException.throwIf(!isSupportedDeviationTarget(deviateSubStmtCtx, targetCtx,
            targetCtx.yangVersion()), deviateSubStmtCtx,
            "%s is not a valid deviation target for substatement %s.", targetCtx.argument(),
            deviateSubStmtCtx.publicDefinition().statementName());
    }

    private static boolean isSupportedDeviationTarget(final StmtContext<?, ?, ?> deviateSubstatementCtx,
            final StmtContext<?, ?, ?> deviateTargetCtx, final YangVersion yangVersion) {
        var supportedDeviationTargets = YangValidationBundles.SUPPORTED_DEVIATION_TARGETS.get(yangVersion,
            deviateSubstatementCtx.publicDefinition());

        if (supportedDeviationTargets == null) {
            supportedDeviationTargets = YangValidationBundles.SUPPORTED_DEVIATION_TARGETS.get(YangVersion.VERSION_1,
                    deviateSubstatementCtx.publicDefinition());
        }

        // if supportedDeviationTargets is null, it means that the deviate substatement is an unknown statement
        return supportedDeviationTargets == null || supportedDeviationTargets.contains(
                deviateTargetCtx.publicDefinition());
    }
}
