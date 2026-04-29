/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.YangDataArchetypeBuilder;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;

/**
 * The {@link Archetype} for {@link YangData} specializations.
 *
 * @since 16.0.0
 */
@Beta
public non-sealed interface YangDataArchetype extends Archetype {
    /**
     * A builder of {@link YangDataArchetype} instances.
     */
    sealed interface Builder extends GeneratedTypeBuilderBase<Builder> permits YangDataArchetypeBuilder {
        @Override
        YangDataArchetype build();
    }

    /**
     * {@return backing {@link YangDataEffectiveStatement}}
     */
    YangDataEffectiveStatement statement();
}
