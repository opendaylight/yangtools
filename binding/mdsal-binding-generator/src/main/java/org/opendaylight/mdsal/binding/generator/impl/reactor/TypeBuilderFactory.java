/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeGeneratedUnion;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;

/**
 * A factory component creating {@link TypeBuilder} instances.
 */
@Beta
public abstract class TypeBuilderFactory implements Immutable {
    static final class Codegen extends TypeBuilderFactory {
        private static final @NonNull Codegen INSTANCE = new Codegen();

        private Codegen() {
            // Hidden on purpose
        }

        @Override
        GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
            return new CodegenGeneratedTOBuilder(identifier);
        }

        @Override
        GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
            return new CodegenGeneratedTypeBuilder(identifier);
        }

        @Override
        AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
            return new CodegenEnumerationBuilder(identifier);
        }

        @Override
        GeneratedUnionBuilder newGeneratedUnionBuilder(final JavaTypeName identifier) {
            return new UnionBuilder(identifier);
        }

        private static final class UnionBuilder extends CodegenGeneratedTOBuilder implements GeneratedUnionBuilder {
            UnionBuilder(final JavaTypeName identifier) {
                super(identifier);
                setIsUnion(true);
            }

            @Override
            public void setTypePropertyNames(final List<String> propertyNames) {
                // No-op, really
                requireNonNull(propertyNames);
            }
        }
    }

    static final class Runtime extends TypeBuilderFactory {
        private static final @NonNull Runtime INSTANCE = new Runtime();

        private Runtime() {
            // Hidden on purpose
        }

        @Override
        GeneratedTOBuilder newGeneratedTOBuilder(final JavaTypeName identifier) {
            return new RuntimeGeneratedTOBuilder(identifier);
        }

        @Override
        GeneratedTypeBuilder newGeneratedTypeBuilder(final JavaTypeName identifier) {
            return new RuntimeGeneratedTypeBuilder(identifier);
        }

        @Override
        AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
            return new RuntimeEnumerationBuilder(identifier);
        }

        @Override
        GeneratedUnionBuilder newGeneratedUnionBuilder(final JavaTypeName identifier) {
            return new UnionBuilder(identifier);
        }

        private static final class UnionBuilder extends RuntimeGeneratedTOBuilder implements GeneratedUnionBuilder {
            private List<String> typePropertyNames;

            UnionBuilder(final JavaTypeName identifier) {
                super(identifier);
                setIsUnion(true);
            }

            @Override
            public void setTypePropertyNames(final List<String> propertyNames) {
                this.typePropertyNames = List.copyOf(propertyNames);
            }

            @Override
            public GeneratedTransferObject build() {
                return typePropertyNames == null || typePropertyNames.isEmpty()
                    ? super.build() : new UnionGTO(this, typePropertyNames);
            }

            private static final class UnionGTO extends GTO implements RuntimeGeneratedUnion {
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
    }

    TypeBuilderFactory() {
        // Hidden on purpose
    }

    public static @NonNull TypeBuilderFactory codegen() {
        return Codegen.INSTANCE;
    }

    public static @NonNull TypeBuilderFactory runtime() {
        return Runtime.INSTANCE;
    }

    abstract @NonNull AbstractEnumerationBuilder newEnumerationBuilder(JavaTypeName identifier);

    abstract @NonNull GeneratedTOBuilder newGeneratedTOBuilder(JavaTypeName identifier);

    abstract @NonNull GeneratedTypeBuilder newGeneratedTypeBuilder(JavaTypeName identifier);

    abstract @NonNull GeneratedUnionBuilder newGeneratedUnionBuilder(JavaTypeName identifier);

}
