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
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
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
    },
    RUNTIME {
        @Override
        <S extends EffectiveStatement<?, ?>> GeneratedTypeBuilder<S> newGeneratedTypeBuilder(
                final JavaTypeName typeName, final S statement) {
            return new RuntimeGeneratedTypeBuilder<>(typeName, statement);
        }
    };

    abstract <S extends EffectiveStatement<?, ?>> @NonNull GeneratedTypeBuilder<S> newGeneratedTypeBuilder(
        @NonNull JavaTypeName typeName, @NonNull S statement);
}
