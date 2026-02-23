/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTOBuilder;

public sealed interface GeneratedUnionBuilder extends GeneratedTOBuilder {

    final class CodegenBuilder extends CodegenGeneratedTOBuilder implements GeneratedUnionBuilder {
        public CodegenBuilder(final JavaTypeName typeName) {
            super(typeName);
            setIsUnion(true);
        }

        @Override
        public void setTypePropertyNames(final List<String> propertyNames) {
            // No-op, really
            requireNonNull(propertyNames);
        }
    }


    final class RuntimeBuilder extends RuntimeGeneratedTOBuilder implements GeneratedUnionBuilder {
        private List<String> typePropertyNames;

        public RuntimeBuilder(final JavaTypeName typeName) {
            super(typeName);
            setIsUnion(true);
        }

        @Override
        public void setTypePropertyNames(final List<String> propertyNames) {
            typePropertyNames = List.copyOf(propertyNames);
        }

        @Override
        public GeneratedTransferObject build() {
            return typePropertyNames == null || typePropertyNames.isEmpty()
                ? super.build() : new UnionGTO(this, typePropertyNames);
        }

        private static final class UnionGTO extends GTO implements UnionTypeObjectArchetype {
            private final @NonNull List<String> typePropertyNames;

            UnionGTO(final RuntimeGeneratedTOBuilder builder, final List<String> typePropertyNames) {
                super(builder);
                this.typePropertyNames = requireNonNull(typePropertyNames);
            }

            @Override
            public List<String> typePropertyNames() {
                return typePropertyNames;
            }
        }
    }

    void setTypePropertyNames(List<String> propertyNames);
}
