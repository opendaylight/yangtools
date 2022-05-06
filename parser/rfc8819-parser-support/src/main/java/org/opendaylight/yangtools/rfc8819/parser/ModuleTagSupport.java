/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatements;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ModuleTagSupport extends AbstractStatementSupport<Tag, ModuleTagStatement, ModuleTagEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR = SubstatementValidator
            .builder(ModuleTagStatements.MODULE_TAG).build();

    public ModuleTagSupport(YangParserConfiguration config) {
        super(ModuleTagStatements.MODULE_TAG, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public Tag parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
        try {
            return Tag.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid '%s' statement %s, its value '%s' is not allowed.",
                    statementName(), getArgumentDefinition().get().getArgumentName(), value);
        }
    }

    @Override
    public void onStatementAdded(StmtContext.@NonNull Mutable<Tag, ModuleTagStatement,
            ModuleTagEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().publicDefinition();
        SourceException.throwIf(YangStmtMapping.MODULE != parentDef && YangStmtMapping.SUBMODULE != parentDef,
                stmt, "Tags may only be defined at root of either a module or a submodule");
    }

    @Override
    protected @NonNull ModuleTagStatement createDeclared(@NonNull BoundStmtCtx<Tag> ctx,
                                                         @NonNull ImmutableList<DeclaredStatement<?>> substatements) {
        return new ModuleTagStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected @NonNull ModuleTagStatement attachDeclarationReference(@NonNull ModuleTagStatement stmt,
                                                                     @NonNull DeclarationReference reference) {
        return new RefModuleTagStatement(stmt, reference);
    }

    @Override
    protected @NonNull ModuleTagEffectiveStatement createEffective(
            EffectiveStmtCtx.@NonNull Current<Tag, ModuleTagStatement> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ModuleTagEffectiveStatementImpl(stmt, substatements);
    }
}
