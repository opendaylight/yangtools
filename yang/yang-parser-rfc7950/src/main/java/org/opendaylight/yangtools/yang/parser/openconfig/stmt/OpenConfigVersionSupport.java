/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.openconfig.stmt;

import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class OpenConfigVersionSupport extends AbstractStatementSupport<SemVer, OpenConfigVersionStatement,
        EffectiveStatement<SemVer, OpenConfigVersionStatement>> {
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
    public void onLinkageDeclared(final Mutable<SemVer, OpenConfigVersionStatement,
            EffectiveStatement<SemVer, OpenConfigVersionStatement>> stmt) {
        stmt.addToNs(SemanticVersionNamespace.class, stmt.getParentContext(), stmt.getStatementArgument());
    }

    @Override
    public OpenConfigVersionStatement createDeclared(final StmtContext<SemVer, OpenConfigVersionStatement, ?> ctx) {
        return new OpenConfigVersionStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<SemVer, OpenConfigVersionStatement> createEffective(
            final StmtContext<SemVer, OpenConfigVersionStatement,
            EffectiveStatement<SemVer, OpenConfigVersionStatement>> ctx) {
        return new OpenConfigVersionEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}