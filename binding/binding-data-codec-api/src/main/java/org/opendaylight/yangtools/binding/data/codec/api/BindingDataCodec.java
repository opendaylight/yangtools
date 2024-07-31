/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Binding/NormalizedNode codec services.
 */
@NonNullByDefault
public interface BindingDataCodec {
    /**
     * Returns the {@link EffectiveModelContext} of this codec.
     *
     * @return the {@link EffectiveModelContext} of this codec
     */
    EffectiveModelContext modelContext();

    /**
     * Return the {@link BindingNormalizedNodeSerializer} facade.
     *
     * @return the {@link BindingNormalizedNodeSerializer} facade
     */
    BindingNormalizedNodeSerializer nodeSerializer();

    /**
     * Return the {@link BindingCodecTree} facade.
     *
     * @return the {@link BindingCodecTree} facade
     */
    BindingCodecTree tree();

    /**
     * Return the {@link BindingNormalizedNodeWriterFactory} facade.
     *
     * @return the {@link BindingNormalizedNodeWriterFactory} facade
     */
    BindingNormalizedNodeWriterFactory writerFactory();
}
