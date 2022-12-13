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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.spec.util.FooChild;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class BindingReflectionsTest {

    @Test
    public void testBindingWithDummyObject() throws Exception {
        assertEquals("ModuleInfoClassName should be equal to string", "test.$YangModuleInfoImpl",
                BindingReflections.getModuleInfoClassName("test"));
        assertEquals("Module info should be empty Set", Collections.emptySet(),
                BindingReflections.loadModuleInfos());
        assertFalse("Should not be RpcType", BindingReflections.isRpcType(DataObject.class));
        assertTrue("Should be BindingClass", BindingReflections.isBindingClass(DataObject.class));
        assertFalse("Should not be Notification", BindingReflections.isNotification(DataObject.class));

        final ChildOf<?> childOf = mock(FooChild.class);
        doReturn(FooChild.class).when(childOf).implementedInterface();
        assertTrue(BindingReflections.isRpcMethod(TestImplementation.class.getDeclaredMethod("rpcMethodTest")));
        assertEquals(TestImplementation.class, BindingReflections.findAugmentationTarget(TestImplementation.class));

        assertEquals(QName.create("test", "test"), BindingReflections.getQName(TestIdentity.VALUE));
    }

    interface TestIdentity extends BaseIdentity {
        QName QNAME = QName.create("test", "test");
        TestIdentity VALUE = () -> TestIdentity.class;

        @Override
        Class<? extends TestIdentity> implementedInterface();
    }

    static final class TestImplementation implements Augmentation<TestImplementation>, RpcService {
        public static final QName QNAME = QName.create("test", "test");

        @SuppressWarnings("static-method")
        ListenableFuture<List<Object>> rpcMethodTest() {
            return null;
        }

        @Override
        public Class<TestImplementation> implementedInterface() {
            return TestImplementation.class;
        }
    }
}