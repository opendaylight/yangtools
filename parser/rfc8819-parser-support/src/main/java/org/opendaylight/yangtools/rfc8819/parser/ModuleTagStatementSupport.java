/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Parser support for {@code module-tag} statement.
 */
@Beta
public final class ModuleTagStatementSupport
        extends AbstractStatementSupport<Tag, ModuleTagStatement, ModuleTagEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(ModuleTagStatement.DEFINITION).build();

    public ModuleTagStatementSupport(final YangParserConfiguration config) {
        super(ModuleTagStatement.DEFINITION, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public String internArgument(final String rawArgument) {
        return rawArgument != null ? rawArgument.intern() : null;
    }

    @Override
    public Tag parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return new Tag(value).intern();
        } catch (final IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid tag value '%s'", value);
        }
    }

    @Override
    public void onStatementAdded(final Mutable<Tag, ModuleTagStatement, ModuleTagEffectiveStatement> stmt) {
        final var parentDef = stmt.coerceParentContext().publicDefinition();
        SourceException.throwIf(YangStmtMapping.MODULE != parentDef && YangStmtMapping.SUBMODULE != parentDef,
                stmt, "Tags may only be defined at root of either a module or a submodule");
    }

    @Override
    protected ModuleTagStatement createDeclared(final BoundStmtCtx<Tag> ctx,
              final ImmutableList<DeclaredStatement<?>> substatements) {
        return new ModuleTagStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected ModuleTagStatement attachDeclarationReference(final ModuleTagStatement stmt,
            final DeclarationReference reference) {
        return new RefModuleTagStatement(stmt, reference);
    }

    @Override
    protected ModuleTagEffectiveStatement createEffective(final Current<Tag, ModuleTagStatement> stmt,
              final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ModuleTagEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
