/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.opendaylight.yangtools.yang.binding.BindingCodec;

public interface AugmentableCodec<P, I> extends BindingCodec<P, I> {

    I deserialize(P input, InstanceIdentifier<?> path);

}
