/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.runtime.api.FeatureRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

@NonNullByDefault
public final class DefaultFeatureRuntimeType extends AbstractRuntimeType<FeatureEffectiveStatement, FeatureArchetype>
        implements FeatureRuntimeType {
    public DefaultFeatureRuntimeType(final FeatureArchetype archetype) {
        super(archetype);
    }
}
