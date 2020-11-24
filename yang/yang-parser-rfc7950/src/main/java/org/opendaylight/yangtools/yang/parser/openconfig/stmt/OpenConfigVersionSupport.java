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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OpenConfigVersionSupport
        extends BaseStatementSupport<SemVer, OpenConfigVersionStatement, OpenConfigVersionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        OpenConfigStatements.OPENCONFIG_VERSION).build();
    private static final OpenConfigVersionSupport INSTANCE = new OpenConfigVersionSupport();

    private OpenConfigVersionSupport() {
        super(OpenConfigStatements.OPENCONFIG_VERSION);
    }

    public static OpenConfigVersionSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public SemVer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SemVer.valueOf(value);
    }

    @Override
    public void onLinkageDeclared(
            final Mutable<SemVer, OpenConfigVersionStatement, OpenConfigVersionEffectiveStatement> stmt) {
        stmt.addToNs(SemanticVersionNamespace.class, stmt.getParentContext(), stmt.getStatementArgument());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OpenConfigVersionStatement createDeclared(final StmtContext<SemVer, OpenConfigVersionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new OpenConfigVersionStatementImpl(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected OpenConfigVersionStatement createEmptyDeclared(
            final StmtContext<SemVer, OpenConfigVersionStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected OpenConfigVersionEffectiveStatement createEffective(
            final Current<SemVer, OpenConfigVersionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OpenConfigVersionEffectiveStatementImpl(stmt, substatements);
    }
}
