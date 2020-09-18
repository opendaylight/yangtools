/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DisplayHintStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DisplayHintStatementSupport
        extends BaseStringStatementSupport<DisplayHintStatement, DisplayHintEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT).build();
    private static final DisplayHintStatementSupport INSTANCE = new DisplayHintStatementSupport();

    private DisplayHintStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.DISPLAY_HINT);
    }

    public static DisplayHintStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<String, DisplayHintStatement, DisplayHintEffectiveStatement> stmt) {
        stmt.addToNs(IetfYangSmiv2Namespace.class, stmt, "Ietf-yang-smiv2 namespace.");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    public DisplayHintStatement createDeclared(final StmtContext<String, DisplayHintStatement, ?> ctx) {
        return new DisplayHintStatementImpl(ctx);
    }

    @Override
    public DisplayHintEffectiveStatement createEffective(
            final StmtContext<String, DisplayHintStatement, DisplayHintEffectiveStatement> ctx) {
        return new DisplayHintEffectiveStatementImpl(ctx);
    }

}