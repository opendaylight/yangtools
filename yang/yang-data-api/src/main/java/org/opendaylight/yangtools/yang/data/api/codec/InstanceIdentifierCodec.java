/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Codec which serializes / deserializes InstanceIdentifier.
 *
 * @param <T> Target type
 */
public interface InstanceIdentifierCodec<T>  extends Codec<T,YangInstanceIdentifier> {

    @Override
    T serialize(YangInstanceIdentifier data);

    @Override
    YangInstanceIdentifier deserialize(T data);
}
