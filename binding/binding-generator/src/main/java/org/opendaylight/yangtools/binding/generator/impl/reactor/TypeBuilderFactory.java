/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.generator.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenUnionTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.DataRootArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.EnumTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeUnionTypeObjectArchetypeBuilder;
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
public abstract sealed class TypeBuilderFactory implements Immutable {
    private static final class Codegen extends TypeBuilderFactory {
        private static final @NonNull Codegen INSTANCE = new Codegen();

        private Codegen() {
            // Hidden on purpose
        }

        @Override
        DataRootArchetype.Builder newDataRootBuilder(final JavaTypeName typeName, final JavaTypeName yangModuleInfo) {
            return new DataRootArchetypeBuilder.Codegen(typeName, yangModuleInfo);
        }

        @Override
        EnumTypeObjectArchetype.Builder newEnumTypeObjectBuilder(final JavaTypeName typeName) {
            return new EnumTypeObjectArchetypeBuilder.Codegen(typeName);
        }

        @Override
        UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(final JavaTypeName identifier) {
            return new CodegenUnionTypeObjectArchetypeBuilder(identifier);
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
    }

    private static final class Runtime extends TypeBuilderFactory {
        private static final @NonNull Runtime INSTANCE = new Runtime();

        private Runtime() {
            // Hidden on purpose
        }

        @Override
        DataRootArchetype.Builder newDataRootBuilder(final JavaTypeName typeName, final JavaTypeName yangModuleInfo) {
            return new DataRootArchetypeBuilder.Runtime(typeName, yangModuleInfo);
        }

        @Override
        EnumTypeObjectArchetype.Builder newEnumTypeObjectBuilder(final JavaTypeName typeName) {
            return new EnumTypeObjectArchetypeBuilder.Runtime(typeName);
        }

        @Override
        UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(final JavaTypeName identifier) {
            return new RuntimeUnionTypeObjectArchetypeBuilder(identifier);
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
    }

    @NonNullByDefault
    public static final TypeBuilderFactory codegen() {
        return Codegen.INSTANCE;
    }

    @NonNullByDefault
    public static final TypeBuilderFactory runtime() {
        return Runtime.INSTANCE;
    }

    @NonNullByDefault
    abstract DataRootArchetype.Builder newDataRootBuilder(JavaTypeName typeName, JavaTypeName yangModuleInfo);

    @NonNullByDefault
    abstract EnumTypeObjectArchetype.Builder newEnumTypeObjectBuilder(JavaTypeName typeName);

    @NonNullByDefault
    abstract UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(JavaTypeName identifier);

    @NonNullByDefault
    abstract GeneratedTOBuilder newGeneratedTOBuilder(JavaTypeName identifier);

    @NonNullByDefault
    abstract GeneratedTypeBuilder newGeneratedTypeBuilder(JavaTypeName identifier);

    abstract void addCodegenInformation(EffectiveStatement<?, ?> stmt, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(ModuleEffectiveStatement stmt, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(DocumentedNode node, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(ModuleGenerator module, EffectiveStatement<?, ?> stmt,
        GeneratedTypeBuilderBase<?> builder);
}
