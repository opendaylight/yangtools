/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class GroupingStatementSupport
        extends AbstractQNameStatementSupport<@NonNull GroupingStatement, GroupingEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(GroupingStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(GroupingStatement.DEF)
            .addAny(ActionStatement.DEF)
            .addAny(AnydataStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .build();

    GroupingStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(GroupingStatement.DEF, StatementPolicy.copyDeclared(
            (copy, current, substatements) ->
                copy.history().isAddedByUses() == current.history().isAddedByUses()
                && copy.getArgument().equals(current.getArgument())), SubtreePolicy.template(), config, validator);
    }

    public static @NonNull GroupingStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new GroupingStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull GroupingStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new GroupingStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, GroupingStatement, GroupingEffectiveStatement> stmt) {
        final var parent = stmt.coerceParentContext();
        // Shadowing check: make sure we do not trample on pre-existing definitions. This catches sibling
        // declarations and parent declarations which have already been declared.
        parent.putToNs(ParserNamespaces.GROUPING, stmt.getArgument(), stmt,
            GroupingStatementSupport::newSourceException);

        final var grandParent = parent.getParentContext();
        if (grandParent == null) {
            // i.e. parent is a top level statement, so stmt is a top-level grouping exported from the defining module
            return;
        }

        // Shadowing check: make sure grandparent does not see a conflicting definition. This is required to
        // ensure that a grouping in child scope does not shadow a grouping in parent scope which occurs later
        // in the text. For that check we need the full declaration of our model.
        final var action = stmt.newInferenceAction(ModelProcessingPhase.STATEMENT_DEFINITION);
        final var gpReq = action.requiresCtx(grandParent, ModelProcessingPhase.STATEMENT_DEFINITION);
        action.apply(new InferenceAction() {
            @Override
            public void apply(final InferenceContext ctx) {
                final var prev = gpReq.resolve(ctx).namespaceItem(ParserNamespaces.GROUPING, stmt.getArgument());
                if (prev != null) {
                    throw newSourceException(prev, stmt);
                }
            }

            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                // No-op
            }
        });
    }

    @NonNullByDefault
    private static SourceException newSourceException(final StmtContext<?, ?, ?> prev,
            final StmtContext<?, ?, ?> next) {
        // TODO: improve the message with coordinates of conflict
        return new SourceException(next, "Duplicate name for grouping %s", next.getArgument());
    }

    @Override
    protected GroupingStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createGrouping(ctx.getArgument(), substatements);
    }

    @Override
    protected GroupingStatement attachDeclarationReference(final GroupingStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateGrouping(stmt, reference);
    }

    @Override
    protected GroupingEffectiveStatement createEffective(final Current<QName, GroupingStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return EffectiveStatements.createGrouping(stmt.declared(), substatements, stmt.getArgument(),
                EffectiveStmtUtils.historyAndStatusFlags(stmt.history(), substatements));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }
}
