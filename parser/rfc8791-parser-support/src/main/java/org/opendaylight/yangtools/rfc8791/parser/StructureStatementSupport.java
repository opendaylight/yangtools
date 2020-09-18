/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class StructureStatementSupport
        extends AbstractQNameStatementSupport<StructureStatement, StructureEffectiveStatement> {
    static final @NonNull ParserNamespace<QName,
        StmtContext<QName, StructureStatement, StructureEffectiveStatement>> NAMESPACE =
            new ParserNamespace<>("structure");

    static final @NonNull NamespaceBehaviour<QName,
        StmtContext<QName, StructureStatement, StructureEffectiveStatement>> BEHAVIOUR =
            NamespaceBehaviour.global(NAMESPACE);

    private StructureStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(StructureStatement.DEFINITION, StatementPolicy.reject(), config, validator);
    }

    static StructureStatementSupport rfc6020(final YangParserConfiguration config) {
        return new StructureStatementSupport(config,
            SubstatementValidator.builder(StructureStatement.DEFINITION)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    static StructureStatementSupport rfc7950(final YangParserConfiguration config) {
        return new StructureStatementSupport(config,
            SubstatementValidator.builder(StructureStatement.DEFINITION)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYDATA)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected StructureStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new StructureStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected StructureStatement attachDeclarationReference(final StructureStatement stmt,
            final DeclarationReference reference) {
        return new RefStructureStatement(stmt, reference);
    }

    @Override
    public boolean isIgnoringConfig() {
        return true;
    }

    @Override
    public void onStatementAdded(final Mutable<QName, StructureStatement, StructureEffectiveStatement> stmt) {
        final var parent = stmt.coerceParentContext();
        if (parent.getParentContext() != null) {
            throw new SourceException(stmt, "Structure may only be used as top-level statement");
        }

        final var name = stmt.getArgument();
        final var prev = parent.namespaceItem(NAMESPACE, name);
        if (prev != null) {
            throw new SourceException(stmt,
                "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared at %s",
                stmt.getRoot().rawArgument(), name, prev.argument(), prev.sourceReference());
        }
        parent.addToNs(NAMESPACE, name, stmt);
    }

    @Override
    protected StructureEffectiveStatement createEffective(final Current<QName, StructureStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final int flags = new EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .toFlags();
        return new StructureEffectiveStatementImpl(stmt, substatements, flags);
    }
}
