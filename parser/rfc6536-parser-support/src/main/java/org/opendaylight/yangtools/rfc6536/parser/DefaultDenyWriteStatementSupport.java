/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyWriteStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMConstants;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DefaultStatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyWriteStatementSupport
        extends AbstractEmptyStatementSupport<DefaultDenyWriteStatement, DefaultDenyWriteEffectiveStatement> {
    public static final @NonNull StatementDefinition RFC8341_DEF = DefaultStatementDefinition.of(
        NACMStatements.DEFAULT_DENY_WRITE.getStatementName().bindTo(NACMConstants.RFC8341_MODULE).intern(),
        DefaultDenyWriteStatement.class, DefaultDenyWriteEffectiveStatement.class);

    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(NACMStatements.DEFAULT_DENY_WRITE).build();

    private final @NonNull StatementDefinition definition;

    public DefaultDenyWriteStatementSupport(final YangParserConfiguration config) {
        this(config, NACMStatements.DEFAULT_DENY_WRITE);
    }

    public DefaultDenyWriteStatementSupport(final YangParserConfiguration config, final StatementDefinition def) {
        super(NACMStatements.DEFAULT_DENY_WRITE, StatementPolicy.contextIndependent(), config, VALIDATOR);
        definition = requireNonNull(def);
    }

    @Override
    public StatementDefinition definition() {
        return definition;
    }

    @Override
    protected DefaultDenyWriteStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new DefaultDenyWriteStatementImpl(substatements, definition);
    }

    @Override
    protected DefaultDenyWriteStatement attachDeclarationReference(final DefaultDenyWriteStatement stmt,
            final DeclarationReference reference) {
        return new RefDefaultDenyWriteStatement(stmt, reference);
    }

    @Override
    protected DefaultDenyWriteEffectiveStatement createEffective(final Current<Empty, DefaultDenyWriteStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DefaultDenyWriteEffectiveStatementImpl(stmt, substatements, definition);
    }
}
