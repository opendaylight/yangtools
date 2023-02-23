/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.mockito.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.Closeable;
import org.junit.jupiter.api.Test;

class DefaultAnswerTest {
    @Test
    void testAnswering() {
        final var mock = mock(Closeable.class);
        final var e = assertThrows(UnstubbedMethodException.class, mock::close);
        assertEquals("closeable.close(); was not stubbed", e.getMessage());
    }
}
