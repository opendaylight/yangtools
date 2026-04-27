/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.IdentityArchetypeBuilder;

/**
 * The {@link Archetype} for {@link BaseIdentity} specializations.
 * @since 16.0.0
 */
public non-sealed interface IdentityArchetype extends Archetype {
    /**
     * A builder of {@link IdentityArchetype} instances.
     */
    @Beta
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder> permits IdentityArchetypeBuilder {
        @Override
        IdentityArchetype build();
    }

    @Override
    default boolean isAbstract() {
        return true;
    }
}
