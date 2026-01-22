/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
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

        for (var baseStmt : StmtContextUtils.findAllDeclaredSubstatements(stmt, BaseStatement.class)) {
            final var baseIdentity = baseStmt.getArgument();
            if (stmt.namespaceItem(ParserNamespaces.IDENTITY, baseIdentity) == null) {
                throw new InferenceException(stmt,
                    "Referenced base identity '%s' doesn't exist in given scope (module, imported modules, submodules)",
                    baseIdentity.getLocalName());
            }
        }
    }

    @Override
    protected IdentityRefSpecification createDeclared(final BoundStmtCtx<QName> ctx,
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
                final var identityQName = bes.argument();
                final var baseIdentity = verifyNotNull(stmt.namespaceItem(ParserNamespaces.IDENTITY, identityQName))
                    .buildEffective();
                if (!(baseIdentity instanceof IdentitySchemaNode isn)) {
                    throw new VerifyException("Statement " + baseIdentity + " is not an IdentitySchemaNode");
                }
                builder.addIdentity(isn);
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
