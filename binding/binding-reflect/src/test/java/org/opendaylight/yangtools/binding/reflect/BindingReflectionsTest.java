/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.reflect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;

class BindingReflectionsTest {
    @Test
    void testBindingWithDummyObject() throws Exception {
        assertFalse(BindingReflections.isRpcType(DataObject.class));
        assertTrue(BindingReflections.isBindingClass(DataObject.class));
    }

    static final class TestImplementation implements Augmentation<TestImplementation> {
        @Override
        public Class<TestImplementation> implementedInterface() {
            return TestImplementation.class;
        }
    }
}
