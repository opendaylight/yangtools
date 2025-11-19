/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

/**
 * A {@link NormalizedYangData.BuilderFactory} producing builders which produce immutable on-heap
 * {@link NormalizedYangData} instances.
 */
@NonNullByDefault
public final class ImmutableYangDataBuilderFactory implements NormalizedYangData.BuilderFactory {
    /**
     * Default constructor.
     */
    public ImmutableYangDataBuilderFactory() {
        // Nothing else
    }

    @Override
    public NormalizedYangData.Builder newYangDataBuilder(final YangDataName name) {
        return new ImmutableYangDataBuilder(name);
    }
}
