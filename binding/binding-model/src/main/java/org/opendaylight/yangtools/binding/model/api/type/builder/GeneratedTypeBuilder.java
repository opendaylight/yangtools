/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Generated Type Builder interface is helper interface for building and defining the {@link LegacyArchetype}.
 *
 * @param <S> EffectiveStatement type
 */
public sealed interface GeneratedTypeBuilder<S extends EffectiveStatement<?, ?>>
        extends GeneratedTypeBuilderBase<GeneratedTypeBuilder<S>>
        permits CodegenGeneratedTypeBuilder, RuntimeGeneratedTypeBuilder {
    @Override
    LegacyArchetype<S> build();
}
