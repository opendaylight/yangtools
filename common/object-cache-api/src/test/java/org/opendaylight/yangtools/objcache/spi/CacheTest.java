/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.objcache.ObjectCache;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.cache.CacheBuilder;

public class CacheTest {
    private FinalizableReferenceQueue queue;
    private ObjectCache oc;

    @Before
    public void setUp() {
        queue = new FinalizableReferenceQueue();
        oc = new AbstractObjectCache(CacheBuilder.newBuilder().softValues().build(), queue) {
        };
    }

    @After
    public void tearDown() {
        queue.close();
    }

    @Test
    public void testMissingKey() {
        final String key1 = "abcd";
        final String key2 = "efgh";

        assertSame(key1, oc.getReference(key1));
        assertSame(key2, oc.getReference(key2));
    }

    @Test
    // This test is based on using different references
    @SuppressWarnings("RedundantStringConstructorCall")
    public void testPresentKey() {
        final String key1 = new String("abcd");
        final String key2 = new String("abcd");

        assertSame(key1, oc.getReference(key1));

        final String key3 = oc.getReference(key2);
        assertEquals(key2, key3);
        assertNotSame(key2, key3);
        assertSame(key1, key3);
    }
}
