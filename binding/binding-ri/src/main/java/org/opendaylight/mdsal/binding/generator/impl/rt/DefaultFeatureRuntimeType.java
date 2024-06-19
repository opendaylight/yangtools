/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.FeatureRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

public final class DefaultFeatureRuntimeType extends AbstractGeneratedRuntimeType<FeatureEffectiveStatement>
        implements FeatureRuntimeType {
    public DefaultFeatureRuntimeType(final GeneratedType bindingType, final FeatureEffectiveStatement statement) {
        super(bindingType, statement);
    }
}
