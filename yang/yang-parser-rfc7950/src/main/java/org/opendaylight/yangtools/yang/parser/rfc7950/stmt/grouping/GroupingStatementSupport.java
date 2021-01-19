/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.grouping;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class GroupingStatementSupport
        extends AbstractQNameStatementSupport<GroupingStatement, GroupingEffectiveStatement> {
    private static final @NonNull GroupingStatementSupport RFC6020_INSTANCE = new GroupingStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build());
    private static final @NonNull GroupingStatementSupport RFC7950_INSTANCE = new GroupingStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.ACTION)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build());

    private final SubstatementValidator validator;

    GroupingStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.GROUPING, CopyPolicy.DECLARED_COPY);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull GroupingStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull GroupingStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<QName, GroupingStatement, GroupingEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final Mutable<?, ?, ?> parent = stmt.getParentContext();
        if (parent != null) {
            // Shadowing check: make sure we do not trample on pre-existing definitions. This catches sibling
            // declarations and parent declarations which have already been declared.
            checkConflict(parent, stmt);
            parent.addContext(GroupingNamespace.class, stmt.getArgument(), stmt);

            final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
            if (grandParent != null) {
                // Shadowing check: make sure grandparent does not see a conflicting definition. This is required to
                // ensure that a grouping in child scope does not shadow a grouping in parent scope which occurs later
                // in the text. For that check we need the full declaration of our model.
                final ModelActionBuilder action = stmt.newInferenceAction(ModelProcessingPhase.FULL_DECLARATION);
                action.requiresCtx(grandParent.getRoot(), ModelProcessingPhase.FULL_DECLARATION);
                action.apply(new InferenceAction() {
                    @Override
                    public void apply(final InferenceContext ctx) {
                        checkConflict(grandParent, stmt);
                    }

                    @Override
                    public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                        // No-op
                    }
                });
            }
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected GroupingStatement createDeclared(final StmtContext<QName, GroupingStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularGroupingStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected GroupingStatement createEmptyDeclared(final StmtContext<QName, GroupingStatement, ?> ctx) {
        return new EmptyGroupingStatement(ctx.getArgument());
    }

    @Override
    protected GroupingEffectiveStatement createEffective(final Current<QName, GroupingStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new GroupingEffectiveStatementImpl(stmt.declared(), substatements,
                EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements), stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static void checkConflict(final StmtContext<?, ?, ?> parent, final StmtContext<QName, ?, ?> stmt) {
        final QName arg = stmt.getArgument();
        final StmtContext<?, ?, ?> existing = parent.getFromNamespace(GroupingNamespace.class, arg);
        SourceException.throwIf(existing != null, stmt, "Duplicate name for grouping %s", arg);
    }
}