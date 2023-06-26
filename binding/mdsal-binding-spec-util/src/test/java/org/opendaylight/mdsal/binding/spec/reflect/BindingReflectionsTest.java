/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;

public class BindingReflectionsTest {

    @Test
    public void testBindingWithDummyObject() throws Exception {
        assertFalse("Should not be RpcType", BindingReflections.isRpcType(DataObject.class));
        assertTrue("Should be BindingClass", BindingReflections.isBindingClass(DataObject.class));
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
