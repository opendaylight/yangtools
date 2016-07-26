/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import java.io.Serializable;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito Answer which for un-stubbed methods throws an
 * UnstubbedMethodException (instead of Mockito's default of returning null).
 *
 * <p>
 * Usage:
 *
 * <pre>
 * import static ...testutils.mockito.Answers2.EXCEPTION;
 *
 * Mockito.mock(YourInterface.class, EXCEPTION)
 * </pre>
 *
 * @see Mockito#mock(Class, Answer)
 *
 * @see ThrowsException
 * @see ThrowsExceptionClass
 *
 * @author Michael Vorburger
 */
public class ThrowsMethodExceptionAnswer implements Answer<Object>, Serializable {
    private static final long serialVersionUID = -7316574192253912318L;

    public static final Answer<Object> EXCEPTION_ANSWER = new ThrowsMethodExceptionAnswer();

    /**
     * Use {@link Answers2} to obtain an instance.
     */
    protected ThrowsMethodExceptionAnswer() {
    }

    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new UnstubbedMethodException(invocation.getMethod());
    }

}
