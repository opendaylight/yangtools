/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class IdentityRefSpecificationSupport extends AbstractTypeSupport {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(TypeStatement.DEF).addMandatory(BaseStatement.DEF).build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(TypeStatement.DEF).addMultiple(BaseStatement.DEF).build();

    private IdentityRefSpecificationSupport(final YangParserConfiguration config,
            final SubstatementValidator validator) {
        super(config, validator);
    }

    static @NonNull IdentityRefSpecificationSupport rfc6020Instance(final YangParserConfiguration config) {
        return new IdentityRefSpecificationSupport(config, RFC6020_VALIDATOR);
    }

    static @NonNull IdentityRefSpecificationSupport rfc7950Instance(final YangParserConfiguration config) {
        return new IdentityRefSpecificationSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, TypeStatement, TypeEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        for (var substatement : stmt.declaredSubstatements()) {
            final var base = substatement.asDeclaring(BaseStatement.DEF);
            if (base == null) {
                continue;
            }

            final var identity = base.getArgument();
            if (stmt.namespaceItem(ParserNamespaces.IDENTITY, identity) == null) {
                throw new InferenceException(stmt,
                    "Referenced base identity '%s' doesn't exist in given scope (module, imported modules, submodules)",
                    // FIXME: report namespace as well
                    identity.getLocalName());
            }
        }
    }

    @Override
    protected TypeStatement.OfIdentityref createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBase(ctx);
        }
        return new IdentityRefSpecificationImpl(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected TypeEffectiveStatement createEffective(final Current<QName, TypeStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noBase(stmt);
        }

        final var builder = BaseTypes.identityrefTypeBuilder(stmt.argumentAsTypeQName());
        for (var subStmt : substatements) {
            if (subStmt instanceof BaseEffectiveStatement bes) {
                builder.addIdentity(stmt.getNamespaceItem(ParserNamespaces.IDENTITY, bes.argument())
                    .buildEffective().toSchemaNode());
            }
        }

        return new TypeEffectiveStatementImpl<>(stmt.declared(), substatements, builder);
    }

    private static SourceException noBase(final CommonStmtCtx stmt) {
        /*
         *  https://www.rfc-editor.org/rfc/rfc7950#section-9.10.2
         *
         *     The "base" statement, which is a substatement to the "type"
         *     statement, MUST be present at least once if the type is
         *     "identityref".
         */
        return new SourceException("At least one base statement has to be present", stmt);
    }
}
