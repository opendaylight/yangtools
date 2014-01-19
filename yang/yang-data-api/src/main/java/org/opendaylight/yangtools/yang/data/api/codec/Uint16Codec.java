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
 * 
 * FIXME: In Helium release this codec should be changed to
 * {@link UnsignedShort}
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint16Codec<T> extends Codec<T,Integer> {

    public T serialize(Integer data);

    public Integer deserialize(T data);
}
