/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

// RFC7950 section 6.2.1:
//        o  All grouping names defined within a parent node or at the top
//           level of the module or its submodules share the same grouping
//           identifier namespace.  This namespace is scoped to all descendant
//           nodes of the parent node or module.  This means that any
//           descendant node may use that grouping, and it MUST NOT define a
//           grouping with the same name.
public final class GroupingStatementSupport
        extends NestedNamespaceStatementSupport<GroupingStatement, GroupingEffectiveStatement> {
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

    private GroupingStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(GroupingStatement.DEF, StatementPolicy.copyDeclared(
            (copy, current, substatements) ->
                copy.history().isAddedByUses() == current.history().isAddedByUses()
                && copy.getArgument().equals(current.getArgument())), SubtreePolicy.template(), config, validator,
            ParserNamespaces.GROUPING);
    }

    public static @NonNull GroupingStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new GroupingStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull GroupingStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new GroupingStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    SourceException newSourceException(final StmtContext<?, ?, ?> prev, final StmtContext<?, ?, ?> next) {
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
