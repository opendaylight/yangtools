/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class MethodExtensionsTest {
    public <T> void fooBar(final int index, final T element) {
        // No-op
    }

    @Test
    void betterToString() throws Exception {
        Method method = MethodExtensionsTest.class.getMethod("fooBar", Integer.TYPE, Object.class);
        assertEquals("fooBar(int index, T element)", MethodExtensions.toString(method));
    }
}
