/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mockito;

import static java.util.Objects.requireNonNull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Additional {@link Answer}s.
 */
public enum MoreAnswers implements Answer<Object> {
    /**
     * An {@link Answer} that throws {@link UnstubbedMethodException}.
     */
    THROWS_UNSTUBBED_METHOD(new ThrowsUnstubbedMethod());

    private final ThrowsUnstubbedMethod implementation;

    MoreAnswers(final ThrowsUnstubbedMethod implementation) {
        this.implementation = requireNonNull(implementation);
    }

    @Override
    public Object answer(final InvocationOnMock invocation) throws Throwable {
        return implementation.answer(invocation);
    }
}
