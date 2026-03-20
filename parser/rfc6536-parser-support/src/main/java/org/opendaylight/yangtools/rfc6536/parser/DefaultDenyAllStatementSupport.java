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
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllEffectiveStatement;
import org.opendaylight.yangtools.rfc6536.model.api.DefaultDenyAllStatement;
import org.opendaylight.yangtools.rfc6536.model.api.NACMStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class DefaultDenyAllStatementSupport
        extends AbstractEmptyStatementSupport<DefaultDenyAllStatement, DefaultDenyAllEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(NACMStatements.DEFAULT_DENY_ALL).build();

    private final @NonNull StatementDefinition definition;

    public DefaultDenyAllStatementSupport(final YangParserConfiguration config, final StatementDefinition definition) {
        super(NACMStatements.DEFAULT_DENY_ALL, StatementPolicy.contextIndependent(), config, VALIDATOR);
        this.definition = requireNonNull(definition);
    }

    @Override
    public StatementDefinition definition() {
        return definition;
    }

    @Override
    protected DefaultDenyAllStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new DefaultDenyAllStatementImpl(substatements, definition);
    }

    @Override
    protected DefaultDenyAllStatement attachDeclarationReference(final DefaultDenyAllStatement stmt,
            final DeclarationReference reference) {
        return new RefDefaultDenyAllStatement(stmt, reference);
    }

    @Override
    protected DefaultDenyAllEffectiveStatement createEffective(final Current<Empty, DefaultDenyAllStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DefaultDenyAllEffectiveStatementImpl(stmt, substatements, definition);
    }
}
