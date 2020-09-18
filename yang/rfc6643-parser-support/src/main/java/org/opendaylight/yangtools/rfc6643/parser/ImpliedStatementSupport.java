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
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class ImpliedStatementSupport
        extends AbstractStatementSupport<String, ImpliedStatement, ImpliedEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.IMPLIED).build();
    private static final ImpliedStatementSupport INSTANCE = new ImpliedStatementSupport();

    private ImpliedStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.IMPLIED);
    }

    public static ImpliedStatementSupport getInstance() {
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
    public ImpliedStatement createDeclared(final StmtContext<String, ImpliedStatement, ?> ctx) {
        return new ImpliedStatementImpl(ctx);
    }

    @Override
    public ImpliedEffectiveStatement createEffective(
            final StmtContext<String, ImpliedStatement, ImpliedEffectiveStatement> ctx) {
        return new ImpliedEffectiveStatementImpl(ctx);
    }
}