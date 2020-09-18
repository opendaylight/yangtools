/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.AliasStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class AliasStatementSupport
        extends AbstractStatementSupport<String, AliasStatement, AliasEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.ALIAS)
                .add(YangStmtMapping.DESCRIPTION, 0, 1)
                .add(YangStmtMapping.REFERENCE, 0, 1)
                .add(YangStmtMapping.STATUS, 0, 1)
                .add(IetfYangSmiv2ExtensionsMapping.OBJECT_ID, 0, 1)
                .build();
    private static final AliasStatementSupport INSTANCE = new AliasStatementSupport();

    private AliasStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.ALIAS);
    }

    public static AliasStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<String, AliasStatement, AliasEffectiveStatement> stmt) {
        stmt.addToNs(IetfYangSmiv2Namespace.class, stmt, "Ietf-yang-smiv2 namespace.");
        getSubstatementValidator().validate(stmt);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public AliasStatement createDeclared(final StmtContext<String, AliasStatement, ?> ctx) {
        return new AliasStatementImpl(ctx);
    }

    @Override
    public AliasEffectiveStatement createEffective(
            final StmtContext<String, AliasStatement, AliasEffectiveStatement> ctx) {
        return new AliasEffectiveStatementImpl(ctx);
    }
}