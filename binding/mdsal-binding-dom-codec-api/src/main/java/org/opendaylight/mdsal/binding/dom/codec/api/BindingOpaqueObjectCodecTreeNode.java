/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;

@Beta
public interface BindingOpaqueObjectCodecTreeNode<T extends OpaqueObject<T>> extends BindingObjectCodecTreeNode<T>,
        BindingNormalizedNodeCodec<T> {

}
