/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public interface BindingCodecTreeFactory {

    /**
     *
     * Creates Binding Codec Tree for specified Binding runtime context.
     *
     * @param context
     *            Binding Runtime Context for which Binding codecs should be
     *            instantiated.
     * @return Binding Codec Tree for specified Binding runtime context.
     */
    BindingCodecTree create(BindingRuntimeContext context);

    /**
     *
     * Creates Binding Codec Tree for specified Binding runtime context.
     *
     * @param context
     *            Binding Runtime Context for which Binding codecs should be
     *            instantiated.
     * @param bindingClasses
     *            Binding Runtime Context will be constructed using bindings
     *            which contains specified classes, in order to support
     *            deserialization in multi-classloader environment.
     * @return Binding Codec Tree for specified Binding runtime context.
     */
    @Beta
   BindingCodecTree create(SchemaContext context, Class<?>... bindingClasses);

}
