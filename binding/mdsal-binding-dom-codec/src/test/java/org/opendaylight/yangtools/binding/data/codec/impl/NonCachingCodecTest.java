/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;

public class NonCachingCodecTest {

    @Test
    public void basicTest() throws Exception {
        final BindingNormalizedNodeCodec codec = mock(BindingNormalizedNodeCodec.class);
        doReturn(null).when(codec).serialize(null);
        doReturn(null).when(codec).deserialize(null);
        final NonCachingCodec nonCachingCodec = new NonCachingCodec<>(codec);
        nonCachingCodec.serialize(null);
        verify(codec).serialize(null);
        nonCachingCodec.deserialize(null);
        verify(codec).deserialize(null);
    }
}