/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.model.api.ObjectIdentifier;
import org.opendaylight.yangtools.rfc6643.model.api.OidEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.OidStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class OidStatementSupport
        extends AbstractStatementSupport<ObjectIdentifier, OidStatement, OidEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.OBJECT_ID).build();
    private static final OidStatementSupport INSTANCE = new OidStatementSupport();

    private OidStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.OBJECT_ID, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static OidStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public ObjectIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return ObjectIdentifier.forString(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx, e, "Invalid object identifier '%s'", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OidStatement createDeclared(final StmtContext<ObjectIdentifier, OidStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new OidIdStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected OidStatement createEmptyDeclared(final StmtContext<ObjectIdentifier, OidStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected OidEffectiveStatement createEffective(final Current<ObjectIdentifier, OidStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OidEffectiveStatementImpl(stmt, substatements);
    }
}