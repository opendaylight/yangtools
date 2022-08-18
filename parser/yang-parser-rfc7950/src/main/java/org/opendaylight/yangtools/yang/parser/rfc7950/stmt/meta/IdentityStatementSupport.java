/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class IdentityStatementSupport
        extends AbstractQNameStatementSupport<IdentityStatement, IdentityEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.IDENTITY)
            .addOptional(YangStmtMapping.BASE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.IDENTITY)
            .addAny(YangStmtMapping.BASE)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .build();

    private IdentityStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.IDENTITY, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull IdentityStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new IdentityStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull IdentityStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new IdentityStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, IdentityStatement, IdentityEffectiveStatement> stmt) {
        final QName qname = stmt.getArgument();
        final StmtContext<?, ?, ?> prev = stmt.getFromNamespace(ParserNamespaces.IDENTITY, qname);
        SourceException.throwIf(prev != null, stmt, "Duplicate identity definition %s", qname);
        stmt.addToNs(ParserNamespaces.IDENTITY, qname, stmt);
    }

    @Override
    protected IdentityStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createIdentity(ctx.getArgument(), substatements);
    }

    @Override
    protected IdentityStatement attachDeclarationReference(final IdentityStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateIdentity(stmt, reference);
    }

    @Override
    protected IdentityEffectiveStatement createEffective(final Current<QName, IdentityStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            return EffectiveStatements.createIdentity(stmt.declared());
        }

        final List<IdentitySchemaNode> identities = new ArrayList<>();
        for (EffectiveStatement<?, ?> substatement : substatements) {
            if (substatement instanceof BaseEffectiveStatement) {
                final QName qname = ((BaseEffectiveStatement) substatement).argument();
                final IdentityEffectiveStatement identity =
                        verifyNotNull(stmt.getFromNamespace(ParserNamespaces.IDENTITY, qname),
                            "Failed to find identity %s", qname)
                        .buildEffective();
                verify(identity instanceof IdentitySchemaNode, "%s is not a IdentitySchemaNode", identity);
                identities.add((IdentitySchemaNode) identity);
            }
        }

        return EffectiveStatements.createIdentity(stmt.declared(), new FlagsBuilder()
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .toFlags(), substatements, ImmutableSet.copyOf(identities));
    }
}
