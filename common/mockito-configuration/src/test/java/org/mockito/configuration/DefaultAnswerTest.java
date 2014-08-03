/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

public class DefaultAnswerTest {
    @Test
    public void testAnswering() throws IOException {
        Closeable mock = Mockito.mock(Closeable.class);
        try {
            mock.close();
            fail();
        } catch (UnstubbedMethodException e) {
            assertEquals("closeable.close(); was not stubbed", e.getMessage());
        }
    }
}
