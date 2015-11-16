/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;

/**
 *
 * FIXME: Should be changed to UnsignedByte
 *
 * @author ttkacik
 *
 * @param <T>
 *          Output type of serialization.
 */
public interface Uint8Codec<T> extends Codec<T,Short> {
    @Override
    T serialize(Short data);

    @Override
    Short deserialize(T data);
}
