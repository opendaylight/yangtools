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
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

abstract class AbstractGeneratedTOBuilder extends AbstractGeneratedTypeBuilder<GeneratedTOBuilder>
        implements GeneratedTOBuilder {

    // FIXME are these three referenced anywhere at runtime?
    private List<GeneratedPropertyBuilder> equalsProperties = Collections.emptyList();
    private List<GeneratedPropertyBuilder> hashProperties = Collections.emptyList();
    private List<GeneratedPropertyBuilder> toStringProperties = Collections.emptyList();
    private GeneratedTransferObject extendsType;
    private boolean isTypedef = false;
    private boolean isUnionType = false;
    private TypeDefinition<?> baseType = null;

    AbstractGeneratedTOBuilder(final Archetype<?> archetype) {
        super(archetype);
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
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper)
            .add("equalsProperties", equalsProperties)
            .add("hashCodeProperties", hashProperties)
            .add("stringProperties", toStringProperties);
    }

    @Override
    public final void setTypedef(final boolean newIsTypedef) {
        isTypedef = newIsTypedef;
    }

    @Override
    public final void setBaseType(final TypeDefinition<?> typeDef) {
        baseType = typeDef;
    }

    @Override
    public final void setIsUnion(final boolean newIsUnion) {
        isUnionType = newIsUnion;
    }

    @Override
    public final boolean isUnion() {
        return isUnionType;
    }

    abstract static class AbstractGeneratedTransferObject extends AbstractGeneratedType implements
            GeneratedTransferObject {

        private final List<GeneratedProperty> equalsProperties;
        private final List<GeneratedProperty> hashCodeProperties;
        private final List<GeneratedProperty> stringProperties;
        private final GeneratedTransferObject extendsType;
        private final boolean isTypedef;
        private final TypeDefinition<?> baseType;
        private final boolean isUnionType;

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
            isUnionType = builder.isUnionType;
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
        public final boolean isUnionType() {
            return isUnionType;
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
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return super.addToStringAttributes(toStringHelper)
                .omitNullValues()
                .add("annotations", getAnnotations())
                .add("comment", getComment())
                .add("extends", getSuperType())
                .add("implements", getImplements())
                .add("enclosedTypes", getEnclosedTypes())
                .add("constants", getConstantDefinitions())
                .add("enumerations", getEnumerations())
                .add("properties", getProperties())
                .add("equalsProperties", equalsProperties)
                .add("hashCodeProperties", hashCodeProperties)
                .add("stringProperties", stringProperties)
                .add("methods", getMethodDefinitions());
        }

        public static final String serializeTypedef(final Type type) {
            if (!(type instanceof ParameterizedType parameterizedType)) {
                return type.getFullyQualifiedName();
            }

            final StringBuilder sb = new StringBuilder();
            sb.append(parameterizedType.getRawType().getFullyQualifiedName()).append('<');
            boolean first = true;
            for (final Type parameter : parameterizedType.getActualTypeArguments()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(serializeTypedef(parameter));
            }
            return sb.append('>').toString();
        }
    }
}
