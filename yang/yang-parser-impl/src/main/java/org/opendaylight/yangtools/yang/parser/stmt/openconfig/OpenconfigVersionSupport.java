/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.openconfig;

import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SemanticVersionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;

public final class OpenconfigVersionSupport extends AbstractStatementSupport<SemVer, OpenconfigVersionStatement,
        EffectiveStatement<SemVer, OpenconfigVersionStatement>> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        SupportedExtensionsMapping.OPENCONFIG_VERSION).build();
    private static final OpenconfigVersionSupport INSTANCE = new OpenconfigVersionSupport();

    private OpenconfigVersionSupport() {
        super(SupportedExtensionsMapping.OPENCONFIG_VERSION);
    }

    public static OpenconfigVersionSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public SemVer parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return SemVer.valueOf(value) ;
    }

    @Override
    public void onLinkageDeclared(final Mutable<SemVer, OpenconfigVersionStatement,
            EffectiveStatement<SemVer, OpenconfigVersionStatement>> stmt) {
        stmt.addToNs(SemanticVersionNamespace.class, stmt.getParentContext(), stmt.getStatementArgument());
    }

    @Override
    public OpenconfigVersionStatement createDeclared(final StmtContext<SemVer, OpenconfigVersionStatement, ?> ctx) {
        return new OpenconfigVersionStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<SemVer, OpenconfigVersionStatement> createEffective(
            final StmtContext<SemVer, OpenconfigVersionStatement,
            EffectiveStatement<SemVer, OpenconfigVersionStatement>> ctx) {
        return new OpenconfigVersionEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}