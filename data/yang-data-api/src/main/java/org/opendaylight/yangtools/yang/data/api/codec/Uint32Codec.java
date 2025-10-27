/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * A codec between Uint32 and some other representation.
 *
 * @param <T> Output type of serialization.
 */
public non-sealed interface Uint32Codec<T> extends IllegalArgumentCodec<T, Uint32> {

}
