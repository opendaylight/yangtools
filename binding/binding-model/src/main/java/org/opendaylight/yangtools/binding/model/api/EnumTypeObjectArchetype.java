/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.EnumTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * The {@link Archetype} for {@link EnumTypeObject} specializations.
 * @since 15.0.0
 */
@Beta
@NonNullByDefault
public non-sealed interface EnumTypeObjectArchetype extends Archetype {
    /**
     * A {@link TypeBuilder} producing {@link EnumTypeObjectArchetype}.
     */
    sealed interface Builder extends TypeBuilder, AnnotableTypeBuilder permits EnumTypeObjectArchetypeBuilder {

        void setDescription(String description);

        void setReference(String reference);

        void setModuleName(String moduleName);

        void setYangSourceDefinition(YangSourceDefinition yangSourceDefinition);

        /**
         * Updates this builder with data from <code>enumTypeDef</code>. Specifically this data represents list
         * of value-name pairs.
         *
         * @param enumTypeDef enum type definition as source of enum data for <code>enumBuilder</code>
         */
        void updateEnumPairsFromEnumTypeDef(EnumTypeDefinition enumTypeDef);

        EnumTypeObjectArchetype build();
    }

    /**
     * {@return the {@link EnumTypeValue}s}
     */
    List<EnumTypeValue> values();

    @Override
    @Deprecated(forRemoval = true)
    default boolean isAbstract() {
        return false;
    }

    @Override
    @Deprecated(forRemoval = true)
    default @Nullable TypeComment getComment() {
        return null;
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }
}
