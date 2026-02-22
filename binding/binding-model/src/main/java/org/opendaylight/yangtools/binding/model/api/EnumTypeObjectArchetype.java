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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * The {@link Archetype} for {@link EnumTypeObject} specializations.
 * @since 15.0.0
 */
@Beta
public interface EnumTypeObjectArchetype extends GeneratedType {
    /**
     * {@return list of the enumeration pairs}
     */
    @NonNull List<Pair> getValues();

    @Override
    default boolean isAbstract() {
        return false;
    }

    @Override
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    default List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

    /**
     * Interface is used for reading enumeration item. It means item's name and its value.
     */
    interface Pair extends DocumentedNode.WithStatus {
        /**
         * {@return the name of the enumeration item as it is specified in the input YANG}
         */
        String getName();

        /**
         * {@return the binding representation for the name of the enumeration item}
         */
        String getMappedName();

        /**
         * {@return the value of the enumeration item}
         */
        int getValue();
    }

    /**
     * A {@link TypeBuilder} producing {@link EnumTypeObjectArchetype}.
     */
    non-sealed interface Builder extends TypeBuilder, AnnotableTypeBuilder {

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

        @NonNull EnumTypeObjectArchetype build();
    }
}
