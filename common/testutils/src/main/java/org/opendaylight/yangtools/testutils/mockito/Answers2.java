/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import org.mockito.AdditionalAnswers;
import org.mockito.Answers;
import org.mockito.stubbing.Answer;

/**
 * More {@link Answer} variants.
 *
 * @see Answers
 * @see AdditionalAnswers
 *
 * @author Michael Vorburger
 */
public class Answers2 {

    public static final CallsRealOrExceptionAnswer REAL_OR_EXCEPTION
        = new CallsRealOrExceptionAnswer();

    public static final ThrowsMethodExceptionAnswer EXCEPTION
        = new ThrowsMethodExceptionAnswer();

    private Answers2() {
    }
}
