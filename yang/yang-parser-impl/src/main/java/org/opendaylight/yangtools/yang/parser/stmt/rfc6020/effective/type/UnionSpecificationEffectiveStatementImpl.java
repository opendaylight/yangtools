/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.UnionSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class UnionSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, UnionSpecification> implements TypeEffectiveStatement<UnionSpecification> {
    private static final Set<String> BUILT_IN_TYPES = ImmutableSet.of(
        TypeUtils.BINARY, TypeUtils.BITS, TypeUtils.BOOLEAN, TypeUtils.DECIMAL64, TypeUtils.EMPTY,
        TypeUtils.ENUMERATION, TypeUtils.IDENTITY_REF, TypeUtils.INSTANCE_IDENTIFIER,
        TypeUtils.INT8, TypeUtils.INT16, TypeUtils.INT32, TypeUtils.INT64, TypeUtils.LEAF_REF, TypeUtils.STRING,
        TypeUtils.UINT8, TypeUtils.UINT16, TypeUtils.UINT32, TypeUtils.UINT64, TypeUtils.UNION);

    private static final Comparator<TypeDefinition<?>> TYPE_SORT_COMPARATOR = new Comparator<TypeDefinition<?>>() {
        @Override
        public int compare(final TypeDefinition<?> o1, final TypeDefinition<?> o2) {
            return Boolean.compare(isBuiltInType(o2), isBuiltInType(o1));
        }
    };

    private static boolean isBuiltInType(final TypeDefinition<?> o1) {
        return BUILT_IN_TYPES.contains(o1.getQName().getLocalName());
    }

    private final List<TypeDefinition<?>> types;

    public UnionSpecificationEffectiveStatementImpl(
            final StmtContext<String, UnionSpecification, EffectiveStatement<String, UnionSpecification>> ctx) {
        super(ctx);

        final List<TypeDefinition<?>> tmp = new ArrayList<>();
        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatementImpl) {
                final TypeDefinition<?> type = ((TypeEffectiveStatementImpl) stmt).buildType();

                Preconditions.checkArgument(!(type instanceof LeafrefTypeDefinition),
                    "Invalid leafref subtype in union %s at %s", ctx, ctx.getStatementSourceReference());
                Preconditions.checkArgument(!(type instanceof EmptyTypeDefinition),
                    "Invalid empty subtype in union %s at %s", ctx, ctx.getStatementSourceReference());

                tmp.add(type);
            }
        }

        // FIXME: this looks wrong, as it does not preserve definition order
        Collections.sort(tmp, TYPE_SORT_COMPARATOR);
        types = ImmutableList.copyOf(tmp);
    }

    @Override
    public TypeDefinitionBuilder<UnionTypeDefinition> newTypeDefinitionBuilder() {
        // TODO Auto-generated method stub
        return new AbstractTypeDefinitionBuilder<UnionTypeDefinition>() {
            @Override
            public UnionTypeDefinition build() {
                // FIXME: this is not quite right
                return UnionType.create(types);
            }
        };
    }
}
