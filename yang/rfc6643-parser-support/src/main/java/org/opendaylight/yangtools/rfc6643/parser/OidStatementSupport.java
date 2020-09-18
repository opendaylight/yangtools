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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class OidStatementSupport
        extends BaseStatementSupport<ObjectIdentifier, OidStatement, OidEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.OBJECT_ID).build();
    private static final OidStatementSupport INSTANCE = new OidStatementSupport();

    private OidStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.OBJECT_ID);
    }

    public static OidStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public ObjectIdentifier parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return ObjectIdentifier.forString(value);
    }

    @Override
    public void onFullDefinitionDeclared(
            final StmtContext.Mutable<ObjectIdentifier, OidStatement, OidEffectiveStatement> stmt) {
        stmt.addToNs(IetfYangSmiv2Namespace.class, stmt, "Ietf-yang-smiv2 namespace.");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected OidStatement createDeclared(final StmtContext<ObjectIdentifier, OidStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new OidIdStatementImpl(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected OidStatement createEmptyDeclared(final StmtContext<ObjectIdentifier, OidStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected OidEffectiveStatement createEffective(
            final StmtContext<ObjectIdentifier, OidStatement, OidEffectiveStatement> ctx,
            final OidStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new OidEffectiveStatementImpl(ctx, substatements);
    }

    @Override
    protected OidEffectiveStatement createEmptyEffective(
            final StmtContext<ObjectIdentifier, OidStatement, OidEffectiveStatement> ctx, final OidStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}