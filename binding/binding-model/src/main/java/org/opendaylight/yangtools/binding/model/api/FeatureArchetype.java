/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.FeatureArchetypeImpl;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * An {@link Archetype} for a {@link YangFeature} generated for a {@link FeatureEffectiveStatement}.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface FeatureArchetype extends Archetype permits FeatureArchetypeImpl {
    @Override
    FeatureEffectiveStatement statement();

    static FeatureArchetype of(final TypeName name, final FeatureEffectiveStatement statement) {
        return new FeatureArchetypeImpl(name, statement);
    }
}
