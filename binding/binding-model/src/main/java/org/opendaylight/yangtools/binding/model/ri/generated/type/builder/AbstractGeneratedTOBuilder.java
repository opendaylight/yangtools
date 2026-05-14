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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public abstract sealed class AbstractGeneratedTOBuilder
        extends AbstractGeneratedTypeBuilder<GeneratedTransferObject.Builder> implements GeneratedTransferObject.Builder
        permits CodegenGeneratedTOBuilder, RuntimeUnionTypeObjectArchetypeBuilder {
    // FIXME are these three referenced anywhere at runtime?
    private GeneratedTransferObject<?> extendsType;
    private boolean isTypedef = false;
    private TypeDefinition<?> baseType = null;

    @NonNullByDefault
    AbstractGeneratedTOBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public final AbstractGeneratedTOBuilder setExtendsType(final GeneratedTransferObject<?> genTransObj) {
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
    protected final AbstractGeneratedTOBuilder thisInstance() {
        return this;
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
    public void setRestrictions(final Restrictions restrictions) {
        // No-op
    }

    @Override
    public void setDescription(final String description) {
        // No-op
    }

    @Override
    public void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public void setReference(final String reference) {
        // No-op
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    public abstract static sealed class AbstractGeneratedTransferObject<T extends TypeObject>
            extends AbstractGeneratedType implements GeneratedTransferObject<T>
            permits RuntimeUnionTO, CodegenGeneratedTOBuilder.GTO {
        private final GeneratedTransferObject<?> extendsType;
        private final boolean isTypedef;
        private final TypeDefinition<?> baseType;

        @Deprecated(since = "16.0.0", forRemoval = true)
        AbstractGeneratedTransferObject(final AbstractGeneratedTOBuilder builder) {
            super(builder);
            extendsType = builder.extendsType;
            isTypedef = builder.isTypedef;
            baseType = builder.baseType;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final boolean isTypedef() {
            return isTypedef;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final TypeDefinition<?> getBaseType() {
            return baseType;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final GeneratedTransferObject<?> getSuperType() {
            return extendsType;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("extends", getSuperType());
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        public static final String serializeTypedef(final Type type) {
            if (!(type instanceof ParameterizedType parameterizedType)) {
                return type.canonicalName();
            }

            final var sb = new StringBuilder();
            sb.append(parameterizedType.getRawType().canonicalName()).append('<');
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
