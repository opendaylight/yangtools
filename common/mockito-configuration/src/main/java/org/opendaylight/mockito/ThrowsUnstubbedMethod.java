/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mockito;

import java.io.Serializable;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * An {@link Answer} that throws {@link UnstubbedMethodException}.
 */
final class ThrowsUnstubbedMethod implements Answer<Object>, Serializable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Object answer(final InvocationOnMock invocation) {
        throw new UnstubbedMethodException(invocation + " was not stubbed");
    }
}
