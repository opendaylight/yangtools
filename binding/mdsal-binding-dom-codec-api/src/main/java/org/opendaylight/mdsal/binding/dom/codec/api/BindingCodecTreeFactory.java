/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import org.opendaylight.binding.runtime.api.BindingRuntimeContext;

public interface BindingCodecTreeFactory {
    /**
     * Creates Binding Codec Tree for specified Binding runtime context.
     *
     * @param context
     *            Binding Runtime Context for which Binding codecs should be
     *            instantiated.
     * @return Binding Codec Tree for specified Binding runtime context.
     */
    BindingCodecTree create(BindingRuntimeContext context);
}
