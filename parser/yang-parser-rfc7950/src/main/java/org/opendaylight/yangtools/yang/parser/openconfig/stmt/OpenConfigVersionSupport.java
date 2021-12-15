/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionEffectiveStatement;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OpenConfigVersionSupport
        extends AbstractStatementSupport<SemVer, OpenConfigVersionStatement, OpenConfigVersionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(OpenConfigStatements.OPENCONFIG_VERSION).build();

    public OpenConfigVersionSupport(final YangParserConfiguration config) {
        super(OpenConfigStatements.OPENCONFIG_VERSION, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public SemVer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SemVer.valueOf(value);
    }

    @Override
    public void onLinkageDeclared(
            final Mutable<SemVer, OpenConfigVersionStatement, OpenConfigVersionEffectiveStatement> stmt) {
        stmt.addToNs(SemanticVersionNamespace.class, stmt.getParentContext(), stmt.argument());
    }

    @Override
    protected OpenConfigVersionStatement createDeclared(final BoundStmtCtx<SemVer> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new OpenConfigVersionStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected OpenConfigVersionStatement attachDeclarationReference(final OpenConfigVersionStatement stmt,
            final DeclarationReference reference) {
        return new RefOpenConfigVersionStatement(stmt, reference);
    }

    @Override
    protected OpenConfigVersionEffectiveStatement createEffective(
            final Current<SemVer, OpenConfigVersionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OpenConfigVersionEffectiveStatementImpl(stmt, substatements);
    }
}
