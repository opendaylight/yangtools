/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

/**
 *
 * Implementations of derived type.
 *
 * This is set of utility classes which implements derived YANG type,
 * preserving original implemented interface instead of {@link ExtendedType}
 * which does not preserve final type of data.
 *
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.DerivedTypes} or
 *             {@link org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes} instead
 */
@Deprecated
public abstract class DerivedType<T extends TypeDefinition<T>> implements TypeDefinition<T> {

    private final ExtendedType definition;
    private final Class<T> publicType;

    DerivedType(final Class<T> publicType, final ExtendedType delegate) {
        this.definition = Preconditions.checkNotNull(delegate);
        this.publicType = Preconditions.checkNotNull(publicType);
    }

    public static TypeDefinition<?> from(final TypeDefinition<?> type) {
        if(type instanceof ExtendedType) {
            return from((ExtendedType) type);
        }
        return type;
    }

    public static TypeDefinition<?> from(final ExtendedType type) {
        TypeDefinition<? extends TypeDefinition<?>> baseType = type;
        while (baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        if (baseType instanceof BinaryTypeDefinition) {
            return new DerivedBinaryType(type);
        }
        if (baseType instanceof BooleanTypeDefinition) {
            return new DerivedBooleanType(type);
        }
        if (baseType instanceof DecimalTypeDefinition) {
            return new DerivedDecimalType(type);
        }
        if (baseType instanceof IdentityrefTypeDefinition) {
            return new DerivedIdentityrefType(type);
        }
        if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return new DerivedInstanceIdentifierType(type);
        }
        if (baseType instanceof IntegerTypeDefinition) {
            return new DerivedIntegerType(type);
        }
        if (baseType instanceof LeafrefTypeDefinition) {
            return new DerivedLeafrefType(type);
        }
        if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return new DerivedUnsignedIntegerType(type);
        }
        if (baseType instanceof StringTypeDefinition) {
            return new DerivedStringType(type);
        }
        if(baseType instanceof UnionTypeDefinition) {
            return new DerivedUnionType(type);
        }
        if(baseType instanceof EnumTypeDefinition) {
            return new DerivedEnumType(type);
        }
        if(baseType instanceof BitsTypeDefinition) {
            return new DerivedBitsType(type);
        }
        throw new IllegalArgumentException("Not supported base type of " + baseType.getClass());
    }

    @Override
    public final QName getQName() {
        return definition.getQName();
    }

    @Override
    public final SchemaPath getPath() {
        return definition.getPath();
    }

    @Override
    public final List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return definition.getUnknownSchemaNodes();
    }

    @Override
    public final String getDescription() {
        return definition.getDescription();
    }

    @Override
    public final String getReference() {
        return definition.getReference();
    }

    @Override
    public final String getUnits() {
        return definition.getUnits();
    }

    @Override
    public final Object getDefaultValue() {
        return definition.getDefaultValue();
    }

    @Override
    public final Status getStatus() {
        return definition.getStatus();
    }

    @Override
    public final T getBaseType() {
        final TypeDefinition<?> base = definition.getBaseType();
        if (publicType.isInstance(base)) {
            return publicType.cast(base);
        } else if (base instanceof ExtendedType) {
            return createDerived((ExtendedType) base);
        }
        throw new IllegalStateException("Unsupported base type.");
    }

    protected ExtendedType delegate() {
        return definition;
    }

    /**
     *
     * Creates derived type from supplied ExtendedType, which will implement
     * proper {@link TypeDefinition} interface.
     *
     * @param base Base definition, which does not implement concrete API
     * @return wrapper which implements proper subinterface of {@link TypeDefinition}.
     */
    abstract T createDerived(ExtendedType base);


}
