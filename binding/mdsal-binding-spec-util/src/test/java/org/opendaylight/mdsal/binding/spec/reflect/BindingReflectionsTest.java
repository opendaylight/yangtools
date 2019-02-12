/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.binding.spec.reflect.BindingReflections.findHierarchicalParent;

import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.spec.util.FooChild;
import org.opendaylight.mdsal.binding.spec.util.GroupingFoo;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class BindingReflectionsTest {

    @Test
    public void testBindingWithDummyObject() throws Exception {
        assertEquals("Package name should be equal to string", "org.opendaylight.yang.gen.v1.test.rev990939",
                BindingReflections.getModelRootPackageName("org.opendaylight.yang.gen.v1.test.rev990939"));
        assertEquals("ModuleInfoClassName should be equal to string", "test.$YangModuleInfoImpl",
                BindingReflections.getModuleInfoClassName("test"));
        assertEquals("Module info should be empty Set", Collections.emptySet(),
                BindingReflections.loadModuleInfos());
        assertFalse("Should not be RpcType", BindingReflections.isRpcType(DataObject.class));
        assertFalse("Should not be AugmentationChild", BindingReflections.isAugmentationChild(DataObject.class));
        assertTrue("Should be BindingClass", BindingReflections.isBindingClass(DataObject.class));
        assertFalse("Should not be Notification", BindingReflections.isNotification(DataObject.class));

        assertNull(findHierarchicalParent(mock(DataObject.class)));
        assertEquals(GroupingFoo.class, BindingReflections.findHierarchicalParent(FooChild.class));
        final ChildOf<?> childOf = mock(FooChild.class);
        doReturn(FooChild.class).when(childOf).getImplementedInterface();
        assertEquals(GroupingFoo.class, BindingReflections.findHierarchicalParent(childOf));
        assertTrue(BindingReflections.isRpcMethod(TestImplementation.class.getDeclaredMethod("rpcMethodTest")));
        assertEquals(TestImplementation.class, BindingReflections.findAugmentationTarget(TestImplementation.class));

        assertEquals(Object.class, BindingReflections.resolveRpcOutputClass(
                TestImplementation.class.getDeclaredMethod("rpcMethodTest")).get());
        assertFalse(BindingReflections.resolveRpcOutputClass(
                TestImplementation.class.getDeclaredMethod("rpcMethodTest2")).isPresent());

        assertEquals(QName.create("test", "test"), BindingReflections.getQName(TestIdentity.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings({ "checkstyle:illegalThrows", "checkstyle:avoidHidingCauseException" })
    public void testPrivateConstructor() throws Throwable {
        assertFalse(BindingReflections.class.getDeclaredConstructor().isAccessible());
        final Constructor<?> constructor = BindingReflections.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private interface TestIdentity extends BaseIdentity {
        @SuppressWarnings("unused")
        QName QNAME = QName.create("test", "test");

    }

    private static final class TestImplementation implements Augmentation<TestImplementation>, RpcService {
        @SuppressWarnings("unused")
        public static final QName QNAME = QName.create("test", "test");

        @SuppressWarnings({ "unused", "static-method" })
        ListenableFuture<List<Object>> rpcMethodTest() {
            return null;
        }

        @SuppressWarnings({ "unused", "static-method" })
        ListenableFuture<?> rpcMethodTest2() {
            return null;
        }
    }
}