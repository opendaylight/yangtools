/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.dynamic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A {@link BindingDataCodec} for dynamic environments. Exposes the underlying {@link BindingRuntimeContext}.
 */
@NonNullByDefault
public interface DynamicBindingDataCodec extends BindingDataCodec {
    /**
     * Return the {@link BindingRuntimeContext} of this codec.
     *
     * @return the {@link BindingRuntimeContext} of this codec
     */
    BindingRuntimeContext runtimeContext();

    @Override
    default EffectiveModelContext modelContext() {
        return runtimeContext().modelContext();
    }
}
