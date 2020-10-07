/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigPosixPatternStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PosixPatternStatementSupport extends
        AbstractStringStatementSupport<OpenConfigPosixPatternStatement, OpenConfigPosixPatternEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenConfigStatements.OPENCONFIG_POSIX_PATTERN).build();

    public PosixPatternStatementSupport(final YangParserConfiguration config) {
        super(OpenConfigStatements.OPENCONFIG_POSIX_PATTERN, StatementPolicy.contextIndependent(), config, VALIDATOR);
    }

    @Override
    protected OpenConfigPosixPatternStatement createDeclared(
             final StmtContext<String, OpenConfigPosixPatternStatement, ?> ctx,
             final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new PosixPatternStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected OpenConfigPosixPatternEffectiveStatement createEffective(
            final Current<String, OpenConfigPosixPatternStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OpenConfigPosixPatternEffectiveStatementImpl(stmt, substatements);
    }

    @Override
    protected OpenConfigPosixPatternStatement attachDeclarationReference(
            final OpenConfigPosixPatternStatement stmt, final DeclarationReference reference) {
        return new RefOpenConfigPosixPatternStatement(stmt, reference);
    }
}
