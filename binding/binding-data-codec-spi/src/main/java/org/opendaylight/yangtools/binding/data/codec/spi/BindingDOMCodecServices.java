/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * Collection of services provided by a Binding-DOM codec instance. This interface serves as an atomic unit for
 * acquiring a consistent set of these services.
 *
 * @deprecated Use {@link BindingDataCodec} instead.
 */
@Deprecated(since = "14.0.2", forRemoval = true)
public interface BindingDOMCodecServices
        extends BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer, BindingCodecTree {

    @NonNull BindingRuntimeContext getRuntimeContext();
}
