/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigRegexpPosixStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class RegexpPosixStatementSupport
        extends AbstractEmptyStatementSupport<OpenConfigRegexpPosixStatement, OpenConfigRegexpPosixEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenConfigStatements.OPENCONFIG_REGEXP_POSIX).build();

    public RegexpPosixStatementSupport(final YangParserConfiguration config) {
        super(OpenConfigStatements.OPENCONFIG_REGEXP_POSIX, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    protected OpenConfigRegexpPosixStatement createDeclared(
             final StmtContext<Empty, OpenConfigRegexpPosixStatement, ?> ctx,
             final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new OpenConfigRegexpPosixStatementImpl(substatements);
    }

    @Override
    protected OpenConfigRegexpPosixEffectiveStatement createEffective(
            final Current<Empty, OpenConfigRegexpPosixStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OpenConfigRegexpPosixEffectiveStatementImpl(stmt, substatements);
    }

    @Override
    protected OpenConfigRegexpPosixStatement attachDeclarationReference(final OpenConfigRegexpPosixStatement stmt,
            final DeclarationReference reference) {
        return new RefOpenConfigRegexpPosixStatement(stmt, reference);
    }
}
