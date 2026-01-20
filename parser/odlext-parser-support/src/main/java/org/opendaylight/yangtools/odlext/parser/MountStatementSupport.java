/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.odlext.model.api.MountEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.MountStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class MountStatementSupport
        extends AbstractEmptyStatementSupport<MountStatement, MountEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(MountStatement.DEFINITION).build();

    public MountStatementSupport(final YangParserConfiguration config) {
        super(MountStatement.DEFINITION, StatementPolicy.exactReplica(), config, VALIDATOR);
    }

    @Override
    protected MountStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new MountStatementImpl(substatements);
    }

    @Override
    protected MountStatement attachDeclarationReference(final MountStatement stmt,
            final DeclarationReference reference) {
        return new RefMountStatement(stmt, reference);
    }

    @Override
    protected MountEffectiveStatement createEffective(final Current<Empty, MountStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new MountEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
