/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.OpaqueObject;

@Beta
public non-sealed interface BindingOpaqueObjectCodecTreeNode<T extends OpaqueObject<T>>
        extends BindingObjectCodecTreeNode, BindingNormalizedNodeCodec<T> {
    /**
     * Returns binding class of interface which represents API of current schema node. The result is same as invoking
     * {@link BindingContract#implementedInterface()} on instance of data.
     *
     * @return interface which defines API of binding representation of data.
     */
    @Override
    Class<T> getBindingClass();
}
