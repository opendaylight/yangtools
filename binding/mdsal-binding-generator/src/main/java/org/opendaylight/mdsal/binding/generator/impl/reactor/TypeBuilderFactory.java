/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeGeneratedUnion;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;

/**
 * A factory component creating {@link TypeBuilder} instances.
 */
@Beta
public abstract class TypeBuilderFactory implements Immutable {
    private static final class Codegen extends TypeBuilderFactory {
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

        @Override
        void addCodegenInformation(final EffectiveStatement<?, ?> stmt,
                final GeneratedTypeBuilderBase<?> builder) {
            if (stmt instanceof DocumentedNode documented) {
                addCodegenInformation(documented, builder);
            }
        }

        @Override
        void addCodegenInformation(final ModuleEffectiveStatement stmt, final GeneratedTypeBuilderBase<?> builder) {
            verify(stmt instanceof Module, "Unexpected module %s", stmt);
            final Module module = (Module) stmt;

            YangSourceDefinition.of(module).ifPresent(builder::setYangSourceDefinition);
            TypeComments.description(module).ifPresent(builder::addComment);
            module.getDescription().ifPresent(builder::setDescription);
            module.getReference().ifPresent(builder::setReference);
        }

        @Override
        void addCodegenInformation(final DocumentedNode node, final GeneratedTypeBuilderBase<?> builder) {
            node.getDescription().map(BindingGeneratorUtil::encodeAngleBrackets).ifPresent(builder::setDescription);
            node.getReference().ifPresent(builder::setReference);
        }

        @Override
        void addCodegenInformation(final ModuleGenerator module, final EffectiveStatement<?, ?> stmt,
                final GeneratedTypeBuilderBase<?> builder) {
            if (stmt instanceof DocumentedNode documented) {
                TypeComments.description(documented).ifPresent(builder::addComment);
                documented.getDescription().ifPresent(builder::setDescription);
                documented.getReference().ifPresent(builder::setReference);
            }
            if (stmt instanceof SchemaNode schema) {
                YangSourceDefinition.of(module.statement(), schema).ifPresent(builder::setYangSourceDefinition);
            }
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

    private static final class Runtime extends TypeBuilderFactory {
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

        @Override
        void addCodegenInformation(final EffectiveStatement<?, ?> stmt, final GeneratedTypeBuilderBase<?> builder) {
            // No-op
        }

        @Override
        void addCodegenInformation(final ModuleEffectiveStatement stmt, final GeneratedTypeBuilderBase<?> builder) {
            // No-op
        }

        @Override
        void addCodegenInformation(final DocumentedNode node, final GeneratedTypeBuilderBase<?> builder) {
            // No-op
        }

        @Override
        void addCodegenInformation(final ModuleGenerator module, final EffectiveStatement<?, ?> stmt,
                final GeneratedTypeBuilderBase<?> builder) {
            // No-op
        }

        private static final class UnionBuilder extends RuntimeGeneratedTOBuilder implements GeneratedUnionBuilder {
            private List<String> typePropertyNames;

            UnionBuilder(final JavaTypeName identifier) {
                super(identifier);
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

    abstract void addCodegenInformation(EffectiveStatement<?, ?> stmt, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(ModuleEffectiveStatement stmt, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(DocumentedNode node, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(ModuleGenerator module, EffectiveStatement<?, ?> stmt,
        GeneratedTypeBuilderBase<?> builder);
}
