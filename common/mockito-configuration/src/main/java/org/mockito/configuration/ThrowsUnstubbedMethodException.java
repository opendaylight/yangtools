/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import java.io.Serializable;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Answer that throws {@link UnstubbedMethodException}.
 */
public class ThrowsUnstubbedMethodException implements Answer<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public Object answer(final InvocationOnMock invocation) throws UnstubbedMethodException {
        throw new UnstubbedMethodException(invocation.toString() + " was not stubbed");
    }
}
