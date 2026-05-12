/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenScalarTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeScalarTypeObjectArchetypeBuilder;

/**
 * An archetype for a {@link ScalarTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public non-sealed interface ScalarTypeObjectArchetype extends GeneratedTransferObject<ScalarTypeObject<?>> {
    /**
     * A builder of {@link ScalarTypeObjectArchetype} instances.
     */
    sealed interface Builder extends GeneratedTransferObject.Builder
            permits CodegenScalarTypeObjectArchetypeBuilder, RuntimeScalarTypeObjectArchetypeBuilder {
        @Override
        ScalarTypeObjectArchetype build();
    }

    @Override
    default long serialVersionUID() {
        return SerialVersionHelper.computeSerialVersion(this);
    }
}
