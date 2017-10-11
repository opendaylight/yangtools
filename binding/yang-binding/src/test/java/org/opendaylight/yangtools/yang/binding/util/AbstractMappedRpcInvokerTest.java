/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.binding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class AbstractMappedRpcInvokerTest {

    @Test
    public void invokeRpcTest() throws Exception {
        final Method methodWithoutInput =
                TestRpcService.class.getDeclaredMethod("methodWithoutInput", RpcService.class);
        final Method methodWithInput =
                TestRpcService.class.getDeclaredMethod("methodWithInput", RpcService.class, DataObject.class);

        methodWithInput.setAccessible(true);
        methodWithoutInput.setAccessible(true);

        final RpcService rpcService = new TestRpcService();

        final RpcServiceInvoker testRpcInvoker =
                new TestRpcInvokerImpl(ImmutableMap.of(
                        "(test)tstWithoutInput", methodWithoutInput,
                        "(test)tstWithInput", methodWithInput));

        final Field testInvokerMapField = testRpcInvoker.getClass().getSuperclass().getDeclaredField("map");
        testInvokerMapField.setAccessible(true);
        final Map<String, RpcMethodInvoker> testInvokerMap =
                (Map<String, RpcMethodInvoker>) testInvokerMapField.get(testRpcInvoker);

        assertTrue(testInvokerMap.get("(test)tstWithInput") instanceof RpcMethodInvokerWithInput);
        assertTrue(testInvokerMap.get("(test)tstWithoutInput") instanceof RpcMethodInvokerWithoutInput);

        final Crate crateWithoutInput =
                (Crate) testRpcInvoker.invokeRpc(rpcService, QName.create("test", "tstWithoutInput"), null).get();
        assertEquals(TestRpcService.methodWithoutInput(rpcService).get().getRpcService(),
                crateWithoutInput.getRpcService());
        assertFalse(crateWithoutInput.getDataObject().isPresent());

        final DataObject dataObject = mock(DataObject.class);
        final Crate crateWithInput =
                (Crate) testRpcInvoker.invokeRpc(rpcService, QName.create("test", "tstWithInput"), dataObject).get();
        assertEquals(TestRpcService.methodWithInput(rpcService, dataObject).get().getRpcService(),
                crateWithInput.getRpcService());
        assertTrue(crateWithInput.getDataObject().isPresent());
        assertEquals(dataObject, crateWithInput.getDataObject().get());
    }

    private class TestRpcInvokerImpl extends AbstractMappedRpcInvoker<String> {

        TestRpcInvokerImpl(final Map<String, Method> map) {
            super(map);
        }

        @Override
        protected String qnameToKey(final QName qname) {
            return qname.toString();
        }
    }

    static class Crate {
        private final RpcService rpcService;
        private final ThreadLocal<Optional<DataObject>> dataObject;

        Crate(@Nonnull final RpcService rpcService, @Nullable final DataObject dataObject) {
            this.rpcService = rpcService;
            this.dataObject = new ThreadLocal<Optional<DataObject>>() {
                @Override
                protected Optional<DataObject> initialValue() {
                    return dataObject == null ? Optional.empty() : Optional.of(dataObject);
                }
            };
        }

        RpcService getRpcService() {
            return this.rpcService;
        }

        Optional<DataObject> getDataObject() {
            return this.dataObject.get();
        }
    }

    static class TestRpcService implements RpcService {
        static Future<Crate> methodWithoutInput(final RpcService testArgument) {
            return Futures.immediateFuture(new Crate(testArgument, null));
        }

        static Future<Crate> methodWithInput(final RpcService testArgument, final DataObject testArgument2) {
            return Futures.immediateFuture(new Crate(testArgument, testArgument2));
        }
    }
}

