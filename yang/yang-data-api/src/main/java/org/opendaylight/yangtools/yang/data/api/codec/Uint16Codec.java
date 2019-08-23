/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * A codec between Uint16 and some other representation.
 *
 * @param <T> Output type of serialization.
 */
public interface Uint16Codec<T> extends Codec<T, Uint16> {
    @Override
    T serialize(Uint16 data);

    @Override
    Uint16 deserialize(T data);
}
