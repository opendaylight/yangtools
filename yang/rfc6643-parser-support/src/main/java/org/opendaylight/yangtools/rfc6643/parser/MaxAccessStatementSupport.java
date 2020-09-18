/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class MaxAccessStatementSupport
        extends AbstractStatementSupport<String, MaxAccessStatement, MaxAccessEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.MAX_ACCESS).build();
    private static final MaxAccessStatementSupport INSTANCE = new MaxAccessStatementSupport();

    private MaxAccessStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.MAX_ACCESS);
    }

    public static MaxAccessStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public MaxAccessStatement createDeclared(final StmtContext<String, MaxAccessStatement, ?> ctx) {
        return new MaxAccessStatementImpl(ctx);
    }

    @Override
    public MaxAccessEffectiveStatement createEffective(
            final StmtContext<String, MaxAccessStatement, MaxAccessEffectiveStatement> ctx) {
        return new MaxAccessEffectiveStatementImpl(ctx);
    }
}