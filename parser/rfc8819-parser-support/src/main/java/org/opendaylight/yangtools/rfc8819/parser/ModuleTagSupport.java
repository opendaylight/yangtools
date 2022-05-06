/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatements;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ModuleTagSupport extends AbstractStatementSupport<Tag, ModuleTagStatement, ModuleTagEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(ModuleTagStatements.MODULE_TAG).build();

    public ModuleTagSupport(final YangParserConfiguration config) {
        super(ModuleTagStatements.MODULE_TAG, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public Tag parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Tag.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid '%s' statement %s, its value '%s' is not allowed.",
                    statementName(), getArgumentDefinition().get().getArgumentName(), value);
        }
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
        return new ModuleTagEffectiveStatementImpl(stmt, substatements);
    }
}
