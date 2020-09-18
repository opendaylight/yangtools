/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6643.model.api.DefValEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DefValStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DefValStatementSupport
        extends AbstractStatementSupport<String, DefValStatement, DefValEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.DEFVAL).build();
    private static final DefValStatementSupport INSTANCE = new DefValStatementSupport();

    private DefValStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.DEFVAL);
    }

    public static DefValStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<String, DefValStatement, DefValEffectiveStatement> stmt) {
        stmt.addToNs(IetfYangSmiv2Namespace.class, stmt, "Ietf-yang-smiv2 namespace.");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public DefValStatementImpl createDeclared(final StmtContext<String, DefValStatement, ?> ctx) {
        return new DefValStatementImpl(ctx);
    }

    @Override
    public DefValEffectiveStatement createEffective(
            final StmtContext<String, DefValStatement, DefValEffectiveStatement> ctx) {
        return new DefValEffectiveStatementImpl(ctx);
    }
}