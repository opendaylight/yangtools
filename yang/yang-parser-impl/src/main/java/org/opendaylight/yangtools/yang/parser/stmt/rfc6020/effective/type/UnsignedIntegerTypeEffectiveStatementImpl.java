/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.RangeRestrictedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class UnsignedIntegerTypeEffectiveStatementImpl<T extends UnsignedIntegerTypeDefinition<?, T>> extends
        DeclaredEffectiveStatementBase<String, TypeStatement> implements TypeEffectiveStatement<TypeStatement> {

    private final T typeDefinition;

    private UnsignedIntegerTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final RangeRestrictedTypeBuilder<T> builder) {
        super(ctx);

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RangeEffectiveStatementImpl) {
                final RangeEffectiveStatementImpl rangeStmt = (RangeEffectiveStatementImpl)stmt;
                builder.setRangeConstraint(rangeStmt, rangeStmt.argument());
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    public static UnsignedIntegerTypeEffectiveStatementImpl<Uint8TypeDefinition> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint8TypeDefinition baseType) {
        return new UnsignedIntegerTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint8Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static UnsignedIntegerTypeEffectiveStatementImpl<Uint16TypeDefinition> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint16TypeDefinition baseType) {
        return new UnsignedIntegerTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint16Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static UnsignedIntegerTypeEffectiveStatementImpl<Uint32TypeDefinition> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint32TypeDefinition baseType) {
        return new UnsignedIntegerTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint32Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    public static UnsignedIntegerTypeEffectiveStatementImpl<Uint64TypeDefinition> create(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final Uint64TypeDefinition baseType) {
        return new UnsignedIntegerTypeEffectiveStatementImpl<>(ctx, RestrictedTypes.newUint64Builder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx)));
    }

    @Nonnull
    @Override
    public UnsignedIntegerTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
