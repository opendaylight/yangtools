/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
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
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

/**
 *
 * Implementations of derived type.
 *
 * This is set of utility classes which implements derived YANG type,
 * preserving original implemented interface instead of {@link ExtendedType}
 * which does not preserve final type of data.
 *
 *  FIXME: Lithium: Should be move to yang-model-util package or deprecated
 * if linking parser is reworked to adhere to base type contract
 */
abstract class NormalizatedDerivedType<T extends TypeDefinition<T>> implements TypeDefinition<T> {

    final ExtendedType definition;
    final Class<T> publicType;

    NormalizatedDerivedType(final Class<T> publicType, final ExtendedType delegate) {
        this.definition = delegate;
        this.publicType = publicType;
    }

    static TypeDefinition<?> from(final ExtendedType type) {
        TypeDefinition<? extends TypeDefinition<?>> baseType = type;
        while (baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        if (baseType instanceof BinaryTypeDefinition) {
            return new DerivedBinary(type);
        }
        if (baseType instanceof BooleanTypeDefinition) {
            return new DerivedBoolean(type);
        }
        if (baseType instanceof DecimalTypeDefinition) {
            return new DerivedDecimal(type);
        }
        if (baseType instanceof IdentityrefTypeDefinition) {
            return new DerivedIdentityref(type);
        }
        if (baseType instanceof InstanceIdentifierTypeDefinition) {
            return new DerivedInstanceIdentifier(type);
        }
        if (baseType instanceof IntegerTypeDefinition) {
            return new DerivedInteger(type);
        }
        if (baseType instanceof LeafrefTypeDefinition) {
            return new DerivedLeafref(type);
        }
        if (baseType instanceof UnsignedIntegerTypeDefinition) {
            return new DerivedUnsignedInteger(type);
        }
        if (baseType instanceof StringTypeDefinition) {
            return new DerivedString(type);
        }
        if(baseType instanceof UnionTypeDefinition) {
            return new DerivedUnion(type);
        }
        if(baseType instanceof EnumTypeDefinition) {
            return new DerivedEnum(type);
        }
        if(baseType instanceof BitsTypeDefinition) {
            return new DerivedBits(type);
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

    abstract T createDerived(ExtendedType base);

    static class DerivedBinary extends NormalizatedDerivedType<BinaryTypeDefinition> implements BinaryTypeDefinition {

        public DerivedBinary(final ExtendedType definition) {
            super(BinaryTypeDefinition.class, definition);
        }

        @Override
        BinaryTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedBinary(base);
        }

        @Override
        public List<LengthConstraint> getLengthConstraints() {
            return definition.getLengthConstraints();
        }
    }

    static class DerivedBoolean extends NormalizatedDerivedType<BooleanTypeDefinition> implements BooleanTypeDefinition {

        public DerivedBoolean(final ExtendedType definition) {
            super(BooleanTypeDefinition.class, definition);
        }

        @Override
        BooleanTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedBoolean(base);
        }
    }

    static class DerivedDecimal extends NormalizatedDerivedType<DecimalTypeDefinition> implements DecimalTypeDefinition {

        public DerivedDecimal(final ExtendedType definition) {
            super(DecimalTypeDefinition.class, definition);
        }

        @Override
        DecimalTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedDecimal(base);
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return definition.getRangeConstraints();
        }

        @Override
        public Integer getFractionDigits() {
            return definition.getFractionDigits();
        }
    }

    static class DerivedIdentityref extends NormalizatedDerivedType<IdentityrefTypeDefinition> implements
            IdentityrefTypeDefinition {

        public DerivedIdentityref(final ExtendedType definition) {
            super(IdentityrefTypeDefinition.class, definition);
        }

        @Override
        IdentityrefTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedIdentityref(base);
        }

        @Override
        public IdentitySchemaNode getIdentity() {
            // FIXME: Is this really correct?
            return getBaseType().getIdentity();
        }
    }

    static class DerivedInstanceIdentifier extends NormalizatedDerivedType<InstanceIdentifierTypeDefinition> implements
            InstanceIdentifierTypeDefinition {

        public DerivedInstanceIdentifier(final ExtendedType definition) {
            super(InstanceIdentifierTypeDefinition.class, definition);
        }

        @Override
        InstanceIdentifierTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedInstanceIdentifier(base);
        }

        @Override
        public RevisionAwareXPath getPathStatement() {
            throw new UnsupportedOperationException("Path statement is not part of instance-identifier type");
        }

        @Override
        public boolean requireInstance() {
            return getBaseType().requireInstance();
        }
    }

    static class DerivedInteger extends NormalizatedDerivedType<IntegerTypeDefinition> implements IntegerTypeDefinition {

        public DerivedInteger(final ExtendedType definition) {
            super(IntegerTypeDefinition.class, definition);
        }

        @Override
        IntegerTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedInteger(base);
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return definition.getRangeConstraints();
        }
    }

    static class DerivedLeafref extends NormalizatedDerivedType<LeafrefTypeDefinition> implements LeafrefTypeDefinition {

        public DerivedLeafref(final ExtendedType definition) {
            super(LeafrefTypeDefinition.class, definition);
        }

        @Override
        LeafrefTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedLeafref(base);
        }

        @Override
        public RevisionAwareXPath getPathStatement() {
            return getBaseType().getPathStatement();
        }
    }

    static class DerivedUnsignedInteger extends NormalizatedDerivedType<UnsignedIntegerTypeDefinition> implements
            UnsignedIntegerTypeDefinition {

        public DerivedUnsignedInteger(final ExtendedType definition) {
            super(UnsignedIntegerTypeDefinition.class, definition);
        }

        @Override
        UnsignedIntegerTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedUnsignedInteger(base);
        }

        @Override
        public List<RangeConstraint> getRangeConstraints() {
            return definition.getRangeConstraints();
        }
    }

    static class DerivedString extends NormalizatedDerivedType<StringTypeDefinition> implements StringTypeDefinition {

        public DerivedString(final ExtendedType definition) {
            super(StringTypeDefinition.class, definition);
        }

        @Override
        StringTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedString(base);
        }

        @Override
        public List<LengthConstraint> getLengthConstraints() {
            return definition.getLengthConstraints();
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return definition.getPatternConstraints();
        }


    }

    static class DerivedUnion extends NormalizatedDerivedType<UnionTypeDefinition> implements UnionTypeDefinition {

        public DerivedUnion(final ExtendedType definition) {
            super(UnionTypeDefinition.class, definition);
        }

        @Override
        UnionTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedUnion(base);
        }

        @Override
        public List<TypeDefinition<?>> getTypes() {
            return getBaseType().getTypes();
        }

    }

    static class DerivedEnum extends NormalizatedDerivedType<EnumTypeDefinition> implements EnumTypeDefinition {

        public DerivedEnum(final ExtendedType definition) {
            super(EnumTypeDefinition.class, definition);
        }

        @Override
        EnumTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedEnum(base);
        }

        @Override
        public List<EnumPair> getValues() {
            return getBaseType().getValues();
        }
    }

    static class DerivedBits extends NormalizatedDerivedType<BitsTypeDefinition> implements BitsTypeDefinition {

        public DerivedBits(final ExtendedType definition) {
            super(BitsTypeDefinition.class, definition);
        }

        @Override
        BitsTypeDefinition createDerived(final ExtendedType base) {
            return new DerivedBits(base);
        }

        @Override
        public List<Bit> getBits() {
            return getBaseType().getBits();
        }
    }
}
