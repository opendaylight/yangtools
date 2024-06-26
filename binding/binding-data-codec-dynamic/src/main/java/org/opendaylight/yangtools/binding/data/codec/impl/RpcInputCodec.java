/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;

/**
 * Marker interface for codecs dealing with RPC input being potentially unmapped. We use this interface to mark both
 * {@link UnmappedRpcInputCodec} and {@link ContainerLikeCodecContext}, which results in bimorphic invocation in
 * {@link BindingNormalizedNodeSerializer#fromNormalizedNodeRpcData()}.
 *
 * <p>
 * Without this interface we could end up with megamorphic invocation, as the two implementations cannot share class
 * hierarchy.
 *
 * @author Robert Varga
 *
 * @param <T> Binding representation of data
 */
interface RpcInputCodec<D extends DataObject> extends BindingNormalizedNodeCodec<D> {

}
