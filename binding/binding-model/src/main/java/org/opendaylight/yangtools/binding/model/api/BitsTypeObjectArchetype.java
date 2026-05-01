/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenBitsTypeObjectArchetypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeBitsTypeObjectArchetypeBuilder;

/**
 * An archetype for a {@link BitsTypeObject}.
 */
@Beta
@NonNullByDefault
public non-sealed interface BitsTypeObjectArchetype extends Archetype, GeneratedTransferObject {
    /**
     * A builder of {@link BitsTypeObjectArchetype} instances.
     */
    sealed interface Builder extends GeneratedTOBuilder
            permits CodegenBitsTypeObjectArchetypeBuilder, RuntimeBitsTypeObjectArchetypeBuilder {
        @Override
        BitsTypeObjectArchetype build();
    }
}
