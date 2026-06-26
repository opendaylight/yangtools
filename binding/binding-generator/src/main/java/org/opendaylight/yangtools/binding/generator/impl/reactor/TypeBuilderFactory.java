/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A factory component creating {@link TypeBuilder} instances.
 */
@Beta
public enum TypeBuilderFactory implements Immutable {
    CODEGEN {
        @Override
        <S extends EffectiveStatement<?, ?>> GeneratedTypeBuilder<S> newGeneratedTypeBuilder(
                final JavaTypeName typeName, final S statement) {
            return new CodegenGeneratedTypeBuilder<>(typeName, statement);
        }

        @Override
        @Deprecated(since = "16.0.0", forRemoval = true)
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
    },
    RUNTIME {
        @Override
        <S extends EffectiveStatement<?, ?>> GeneratedTypeBuilder<S> newGeneratedTypeBuilder(
                final JavaTypeName typeName, final S statement) {
            return new RuntimeGeneratedTypeBuilder<>(typeName, statement);
        }

        @Override
        @Deprecated(since = "16.0.0", forRemoval = true)
        void addCodegenInformation(final ModuleGenerator module, final EffectiveStatement<?, ?> stmt,
                final GeneratedTypeBuilderBase<?> builder) {
            // No-op
        }
    };

    abstract <S extends EffectiveStatement<?, ?>> @NonNull GeneratedTypeBuilder<S> newGeneratedTypeBuilder(
        @NonNull JavaTypeName typeName, @NonNull S statement);

    @Deprecated(since = "16.0.0", forRemoval = true)
    abstract void addCodegenInformation(@NonNull ModuleGenerator module, @NonNull EffectiveStatement<?, ?> stmt,
        @NonNull GeneratedTypeBuilderBase<?> builder);
}
