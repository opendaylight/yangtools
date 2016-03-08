/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.objcache.spi.AbstractObjectCache.SoftKey;

import com.google.common.base.FinalizableReferenceQueue;

public class SoftKeyTest {
    private FinalizableReferenceQueue queue;


    @Before
    public void setUp() {
        queue = new FinalizableReferenceQueue();
    }

    @After
    public void tearDown() {
        queue.close();
    }

    @Test
    public void testEquals() {
        final String str = "foo";

        final SoftKey<?> key = new SoftKey<String>(str, queue) {
            @Override
            public void finalizeReferent() {

            }
        };

        assertSame(str, key.get());
        assertEquals(str.hashCode(), key.hashCode());
        assertEquals(key, str);
        key.clear();
        assertNull(key.get());
        assertEquals(str.hashCode(), key.hashCode());
        assertNotEquals(key, str);
    }
}
