/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;

/**
 * Collection of services provided by a Binding-DOM codec instance. This interface serves as an atomic unit for
 * acquiring a consistent set of these services.
 */
@Beta
public interface BindingDOMCodecServices extends BindingNormalizedNodeWriterFactory, BindingNormalizedNodeSerializer,
    BindingCodecTree {

    @NonNull BindingRuntimeContext getRuntimeContext();
}
