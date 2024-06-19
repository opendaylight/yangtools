/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public class RpcServiceInvokerTest {

    @Test
    public void fromTest() throws Exception {
        final Method method = this.getClass().getDeclaredMethod("testMethod");
        method.setAccessible(true);
        assertNotNull(RpcServiceInvoker.from(ImmutableMap.of(
            QName.create(QNameModule.create(URI.create("testURI"), Revision.of("2017-10-26")),"test"), method,
            QName.create(QNameModule.create(URI.create("testURI2"), Revision.of("2017-10-26")),"test"), method)));
        assertNotNull(RpcServiceInvoker.from(ImmutableMap.of(
            QName.create(QNameModule.create(URI.create("testURI"), Revision.of("2017-10-26")), "test"), method)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromWithExceptionTest() {
        RpcServiceInvoker.from(RpcService.class);
        fail("Expected IllegalArgumentException");
    }

    private void testMethod() {
        // NOOP
    }
}
