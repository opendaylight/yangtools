/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public abstract sealed class AbstractGeneratedTOBuilder extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder>
        implements GeneratedTOBuilder permits CodegenGeneratedTOBuilder, RuntimeGeneratedTOBuilder {
    // FIXME are these three referenced anywhere at runtime?
    private List<GeneratedPropertyBuilder> equalsProperties = List.of();
    private List<GeneratedPropertyBuilder> hashProperties = List.of();
    private List<GeneratedPropertyBuilder> toStringProperties = List.of();
    private GeneratedTransferObject extendsType;
    private boolean isTypedef = false;
    private TypeDefinition<?> baseType = null;

    @NonNullByDefault
    AbstractGeneratedTOBuilder(final JavaTypeName typeName) {
        super(typeName);
        setAbstract(false);
    }

    @Override
    public final GeneratedTOBuilder setExtendsType(final GeneratedTransferObject genTransObj) {
        Preconditions.checkArgument(genTransObj != null, "Generated Transfer Object cannot be null!");
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
    public final MethodSignatureBuilder addMethod(final String name) {
        final MethodSignatureBuilder builder = super.addMethod(name);
        builder.setAbstract(false);
        return builder;
    }

    @Override
    public final GeneratedTOBuilder addEqualsIdentity(final GeneratedPropertyBuilder property) {
        equalsProperties = LazyCollections.lazyAdd(equalsProperties, property);
        return this;
    }

    @Override
    public final GeneratedTOBuilder addHashIdentity(final GeneratedPropertyBuilder property) {
        hashProperties = LazyCollections.lazyAdd(hashProperties, property);
        return this;
    }

    @Override
    public final GeneratedTOBuilder addToStringProperty(final GeneratedPropertyBuilder property) {
        toStringProperties = LazyCollections.lazyAdd(toStringProperties, property);
        return this;
    }

    @Override
    protected final GeneratedTOBuilder thisInstance() {
        return this;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);

        addToStringAttribute(helper, "equalsProperties", equalsProperties);
        addToStringAttribute(helper, "hashCodeProperties", hashProperties);
        addToStringAttribute(helper, "stringProperties", toStringProperties);

        return helper;
    }

    @Override
    public final void setTypedef(final boolean newIsTypedef) {
        isTypedef = newIsTypedef;
    }

    @Override
    public final void setBaseType(final TypeDefinition<?> typeDef) {
        baseType = typeDef;
    }

    abstract static class AbstractGeneratedTransferObject extends AbstractGeneratedType
            implements GeneratedTransferObject {
        private final List<GeneratedProperty> equalsProperties;
        private final List<GeneratedProperty> hashCodeProperties;
        private final List<GeneratedProperty> stringProperties;
        private final GeneratedTransferObject extendsType;
        private final boolean isTypedef;
        private final TypeDefinition<?> baseType;

        AbstractGeneratedTransferObject(final AbstractGeneratedTOBuilder builder) {
            super(builder);
            extendsType = builder.extendsType;

            // FIXME: if these fields were guaranteed to be constant, we could perhaps
            //        cache and reuse them between instances...
            equalsProperties = toUnmodifiableProperties(builder.equalsProperties);
            hashCodeProperties = toUnmodifiableProperties(builder.hashProperties);
            stringProperties = toUnmodifiableProperties(builder.toStringProperties);

            isTypedef = builder.isTypedef;
            baseType = builder.baseType;
        }

        @Override
        public final boolean isTypedef() {
            return isTypedef;
        }

        @Override
        public final TypeDefinition<?> getBaseType() {
            return baseType;
        }

        @Override
        public final GeneratedTransferObject getSuperType() {
            return extendsType;
        }

        @Override
        public final List<GeneratedProperty> getEqualsIdentifiers() {
            return equalsProperties;
        }

        @Override
        public final List<GeneratedProperty> getHashCodeIdentifiers() {
            return hashCodeProperties;
        }

        @Override
        public final List<GeneratedProperty> getToStringIdentifiers() {
            return stringProperties;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            super.addToStringAttributes(helper).add("extends", getSuperType());

            addToStringAttribute(helper, "equalsProperties", equalsProperties);
            addToStringAttribute(helper, "hashCodeProperties", hashCodeProperties);
            addToStringAttribute(helper, "stringProperties", stringProperties);

            return helper;
        }

        public static final String serializeTypedef(final Type type) {
            if (!(type instanceof ParameterizedType parameterizedType)) {
                return type.name().canonicalName();
            }

            final var sb = new StringBuilder();
            sb.append(parameterizedType.getRawType().name().canonicalName()).append('<');
            boolean first = true;
            for (var parameter : parameterizedType.getActualTypeArguments()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(serializeTypedef(parameter));
            }
            return sb.append('>').toString();
        }
    }
}
