/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigHashedValueStatement;
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

abstract class AbstractHashedValueStatementSupport
        extends AbstractEmptyStatementSupport<OpenConfigHashedValueStatement, OpenConfigHashedValueEffectiveStatement> {
    AbstractHashedValueStatementSupport(final StatementDefinition definition, final YangParserConfiguration config,
            final SubstatementValidator validator) {
        super(definition, StatementPolicy.contextIndependent(), config, validator);
    }

    @Override
    protected final OpenConfigHashedValueStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement> substatements) {
        return new OpenConfigHashedValueStatementImpl(getPublicView(), substatements);
    }

    @Override
    protected final OpenConfigHashedValueStatement attachDeclarationReference(
            final OpenConfigHashedValueStatement stmt, final DeclarationReference reference) {
        return new RefOpenConfigHashedValueStatement(stmt, reference);
    }

    @Override
    protected OpenConfigHashedValueEffectiveStatement createEffective(
            final Current<Empty, OpenConfigHashedValueStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OpenConfigHashedValueEffectiveStatementImpl(stmt, substatements);
    }
}
