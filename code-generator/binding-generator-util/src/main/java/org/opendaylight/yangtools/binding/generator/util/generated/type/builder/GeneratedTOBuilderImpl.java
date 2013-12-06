/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;

public final class GeneratedTOBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder> implements
        GeneratedTOBuilder {

    private GeneratedTransferObject extendsType;
    private final List<GeneratedPropertyBuilder> equalsProperties = new ArrayList<>();
    private final List<GeneratedPropertyBuilder> hashProperties = new ArrayList<>();
    private final List<GeneratedPropertyBuilder> toStringProperties = new ArrayList<>();
    private boolean isTypedef = false;
    private boolean isUnionType = false;
    private boolean isUnionTypeBuilder = false;
    private Restrictions restrictions;
    private GeneratedPropertyBuilder SUID;

    public GeneratedTOBuilderImpl(String packageName, String name) {
        super(packageName, name);
        setAbstract(false);
    }

    @Override
    public GeneratedTOBuilder setExtendsType(final GeneratedTransferObject genTransObj) {
        if (genTransObj == null) {
            throw new IllegalArgumentException("Generated Transfer Object cannot be null!");
        }
        extendsType = genTransObj;
        return this;
    }

    /**
     * Add new Method Signature definition for Generated Type Builder and
     * returns Method Signature Builder for specifying all Method parameters. <br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code>
     * the method SHOULD throw {@link IllegalArgumentException} <br>
     * By <i>Default</i> the MethodSignatureBuilder SHOULD be pre-set as
     * {@link MethodSignatureBuilder#setAbstract(boolean)},
     * {TypeMemberBuilder#setFinal(boolean)} and
     * {TypeMemberBuilder#setAccessModifier(boolean)}
     *
     * @param name
     *            Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    @Override
    public MethodSignatureBuilder addMethod(String name) {
        final MethodSignatureBuilder builder = super.addMethod(name);
        builder.setAbstract(false);
        return builder;
    }

    @Override
    public GeneratedTOBuilder addEqualsIdentity(GeneratedPropertyBuilder property) {
        equalsProperties.add(property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addHashIdentity(GeneratedPropertyBuilder property) {
        hashProperties.add(property);
        return this;
    }

    @Override
    public GeneratedTOBuilder addToStringProperty(GeneratedPropertyBuilder property) {
        toStringProperties.add(property);
        return this;
    }

    @Override
    protected GeneratedTOBuilder thisInstance() {
        return this;
    }

    @Override
    public void setRestrictions(Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void setSUID(GeneratedPropertyBuilder suid) {
        this.SUID = suid;
    }

    @Override
    public GeneratedTransferObject toInstance() {
        return new GeneratedTransferObjectImpl(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedTransferObject [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append(", constants=");
        builder.append(getConstants());
        builder.append(", enumerations=");
        builder.append(getEnumerations());
        builder.append(", equalsProperties=");
        builder.append(equalsProperties);
        builder.append(", hashCodeProperties=");
        builder.append(hashProperties);
        builder.append(", stringProperties=");
        builder.append(toStringProperties);
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append(", methods=");
        builder.append(getMethodDefinitions());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void setTypedef(boolean isTypedef) {
        this.isTypedef = isTypedef;
    }

    @Override
    public void setIsUnion(boolean isUnion) {
        this.isUnionType = isUnion;
    }

    @Override
    public void setIsUnionBuilder(boolean isUnionTypeBuilder) {
        this.isUnionTypeBuilder = isUnionTypeBuilder;
    }

    private static final class GeneratedTransferObjectImpl extends AbstractGeneratedType implements
            GeneratedTransferObject {

        private final List<GeneratedProperty> equalsProperties;
        private final List<GeneratedProperty> hashCodeProperties;
        private final List<GeneratedProperty> stringProperties;
        private final GeneratedTransferObject extendsType;
        private final boolean isTypedef;
        private final boolean isUnionType;
        private final boolean isUnionTypeBuilder;
        private final Restrictions restrictions;
        private final GeneratedProperty SUID;

        public GeneratedTransferObjectImpl(GeneratedTOBuilderImpl builder) {
            super(builder);
            this.extendsType = builder.extendsType;
            this.equalsProperties = toUnmodifiableProperties(builder.equalsProperties);
            this.hashCodeProperties = toUnmodifiableProperties(builder.hashProperties);
            this.stringProperties = toUnmodifiableProperties(builder.toStringProperties);
            this.isTypedef = builder.isTypedef;
            this.isUnionType = builder.isUnionType;
            this.isUnionTypeBuilder = builder.isUnionTypeBuilder;
            this.restrictions = builder.restrictions;
            if (builder.SUID == null) {
                this.SUID = null;
            } else {
                this.SUID = builder.SUID.toInstance(GeneratedTransferObjectImpl.this);
            }
        }

        @Override
        public boolean isTypedef() {
            return isTypedef;
        }

        @Override
        public boolean isUnionType() {
            return isUnionType;
        }

        @Override
        public boolean isUnionTypeBuilder() {
            return isUnionTypeBuilder;
        }

        @Override
        public GeneratedTransferObject getSuperType() {
            return extendsType;
        }

        @Override
        public List<GeneratedProperty> getEqualsIdentifiers() {
            return equalsProperties;
        }

        @Override
        public List<GeneratedProperty> getHashCodeIdentifiers() {
            return hashCodeProperties;
        }

        @Override
        public List<GeneratedProperty> getToStringIdentifiers() {
            return stringProperties;
        }

        @Override
        public Restrictions getRestrictions() {
            return restrictions;
        }

        @Override
        public GeneratedProperty getSUID() {
            return SUID;
        }

        @Override
        public String toString() {
            if(isTypedef) {
                return serializeTypedef(this);
            }
            StringBuilder builder = new StringBuilder();
            builder.append("GeneratedTransferObject [packageName=");
            builder.append(getPackageName());
            builder.append(", name=");
            builder.append(getName());
            builder.append(", comment=");
            builder.append(", annotations=");
            builder.append(getAnnotations());
            builder.append(getComment());
            builder.append(", extends=");
            builder.append(getSuperType());
            builder.append(", implements=");
            builder.append(getImplements());
            builder.append(", enclosedTypes=");
            builder.append(getEnclosedTypes());
            builder.append(", constants=");
            builder.append(getConstantDefinitions());
            builder.append(", enumerations=");
            builder.append(getEnumerations());
            builder.append(", properties=");
            builder.append(getProperties());
            builder.append(", equalsProperties=");
            builder.append(equalsProperties);
            builder.append(", hashCodeProperties=");
            builder.append(hashCodeProperties);
            builder.append(", stringProperties=");
            builder.append(stringProperties);
            builder.append(", methods=");
            builder.append(getMethodDefinitions());
            builder.append("]");
            return builder.toString();
        }

        public String serializeTypedef(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                StringBuffer sb = new StringBuffer();
                sb.append(parameterizedType.getRawType().getFullyQualifiedName());
                sb.append("<");
                boolean first = true;
                for (Type parameter : parameterizedType.getActualTypeArguments()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(",");
                    }
                    sb.append(serializeTypedef(parameter));
                }
                sb.append(">");
                return sb.toString();
            } else {
                return type.getFullyQualifiedName();
            }
        }

    }
}
