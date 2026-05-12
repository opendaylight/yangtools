/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTOBuilder.AbstractGeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenScalarTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenUnionTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeScalarTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeUnionTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
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
        ScalarTypeObjectArchetype.Builder newScalarTypeObjectBuilder(final JavaTypeName identifier) {
            return new CodegenScalarTypeObjectArchetypeBuilder(identifier);
        }

        @Override
        UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(final JavaTypeName identifier) {
            return new CodegenUnionTypeObjectArchetypeBuilder(identifier);
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
        void addCodegenInformation(final DocumentedNode node, final GeneratedTypeBuilderBase<?> builder) {
            node.getDescription().map(DocUtils::encodeAngleBrackets).ifPresent(builder::setDescription);
            node.getReference().ifPresent(builder::setReference);
        }

        @Override
        void addCodegenInformation(final ModuleGenerator module, final EffectiveStatement<?, ?> stmt,
                final GeneratedTypeBuilderBase<?> builder) {
            if (stmt instanceof DocumentedNode documented) {
                final var comment = DocUtils.typeCommentOf(documented);
                if (comment != null) {
                    builder.addComment(comment);
                }
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
        ScalarTypeObjectArchetype.Builder newScalarTypeObjectBuilder(final JavaTypeName identifier) {
            return new RuntimeScalarTypeObjectArchetypeBuilder(identifier);
        }

        @Override
        UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(final JavaTypeName identifier) {
            return new RuntimeUnionTypeObjectArchetypeBuilder(identifier);
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
    abstract ScalarTypeObjectArchetype.Builder newScalarTypeObjectBuilder(JavaTypeName identifier);

    @NonNullByDefault
    abstract UnionTypeObjectArchetype.Builder newUnionTypeObjectBuilder(JavaTypeName identifier);

    @NonNullByDefault
    abstract GeneratedTypeBuilder newGeneratedTypeBuilder(JavaTypeName identifier);

    @NonNullByDefault
    final GeneratedTransferObject.Builder newTOBuilder(final JavaTypeName typeName,
            final GeneratedTransferObject<?> to) {
        return switch (to) {
            case BitsTypeObjectArchetype bits -> throw new VerifyException("Should never be called with " + bits);
            case ScalarTypeObjectArchetype scalar -> newScalarTypeObjectBuilder(typeName);
            case UnionTypeObjectArchetype union ->
                newUnionTypeObjectBuilder(typeName).setTypePropertyNames(union.typePropertyNames());
            case AbstractGeneratedTransferObject<?> gto -> throw new VerifyException("Unsupported " + gto);
        };
    }

    abstract void addCodegenInformation(EffectiveStatement<?, ?> stmt, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(DocumentedNode node, GeneratedTypeBuilderBase<?> builder);

    abstract void addCodegenInformation(ModuleGenerator module, EffectiveStatement<?, ?> stmt,
        GeneratedTypeBuilderBase<?> builder);
}
