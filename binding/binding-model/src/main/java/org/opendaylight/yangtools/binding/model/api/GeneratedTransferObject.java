/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTOBuilder.AbstractGeneratedTransferObject;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Common interface for {@link TypeObjectArchetype}s other than {@link EnumTypeObject}.
 *
 * <p>Generated Transfer Object extends {@link GeneratedType} and is designed to represent Java Class. The Generated
 * Transfer Object contains declarations of member fields stored in List of Properties. The Generated Transfer Object
 * can be extended by exactly ONE Generated Transfer Object as Java does not allow multiple inheritance. For retrieval
 * of implementing Generated Types use {@link #getImplements()} method.
 *
 * @param <T> {@link TypeObject} specialization
 */
// FIXME: rename to TOArchetype or similar
// FIXME: update documentation
public sealed interface GeneratedTransferObject<T extends TypeObject> extends TypeObjectArchetype<T>
        permits BitsTypeObjectArchetype, ScalarTypeObjectArchetype, UnionTypeObjectArchetype,
                AbstractGeneratedTransferObject {
    /**
     * A builder of {@link GeneratedTransferObject} instances.
     */
    @Beta
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder>
            permits AbstractGeneratedTOBuilder, ScalarTypeObjectArchetype.Builder, UnionTypeObjectArchetype.Builder {
        /**
         * Add Generated Transfer Object from which will be extended current Generated Transfer Object.<br>
         * By definition Java does not allow multiple inheritance, hence if there is already a definition
         * of an Generated Transfer Object the extending object will be overwritten by lastly added Generated Transfer
         * Object.<br>
         * If Generated Transfer Object is <code>null</code> the method SHOULD throw {@link IllegalArgumentException}
         *
         * @param genTransObj Generated Transfer Object
         * @return This instance of builder
         */
        Builder setExtendsType(GeneratedTransferObject<?> genTransObj);

        @NonNullByDefault
        void setRestrictions(Restrictions restrictions);

        void setTypedef(boolean isTypedef);

        /**
         * Sets the base type for Java representation of YANG typedef.
         *
         * @param typeDef Type Definition
         */
        void setBaseType(TypeDefinition<?> typeDef);

        /**
         * Returns instance of <code>GeneratedTransferObject</code> which data are build from the data of this builder.
         *
         * @return generated transfer object instance
         */
        @Override
        GeneratedTransferObject<?> build();
    }

    /**
     * {@return the value of the {@code serialVersionUID} of this {@link TypeObject} class};
     */
    default long serialVersionUID() {
        return SerialVersionHelper.computeSerialVersion(this);
    }

    /**
     * Returns the Generated Transfer Object from which this GTO is derived, or null if this GTO is not derived
     * from a GTO -- e.g. it is either an union or it is derived from a concrete type.
     *
     * @return Generated Transfer Object or <code>null</code> if this GTO is not derived from another GTO.
     */
    @Nullable GeneratedTransferObject<?> getSuperType();

    // FIXME: why do we need this boolean?
    boolean isTypedef();

    /**
     * Returns Base type of Java representation of YANG typedef if set, otherwise it returns null.
     *
     * @return Base type of Java representation of YANG typedef if set, otherwise it returns null
     */
    @Nullable TypeDefinition<?> getBaseType();

    default Restrictions getRestrictions() {
        throw new UnsupportedOperationException("Not available at runtime");
    }

    default Optional<? extends GeneratedProperty> findProperty(final String name) {
        final var optProp = getProperties().stream().filter(prop -> name.equals(prop.getName())).findFirst();
        if (optProp.isPresent()) {
            return optProp;
        }

        final var parent = getSuperType();
        return parent != null ? parent.findProperty(name) : Optional.empty();
    }
}
