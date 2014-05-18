/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.ObjectCacheFactory;

public class CacheFactoryTest {

    @Test
    public void testInvalidEnvironment() {
        final ObjectCache oc = ObjectCacheFactory.getObjectCache(String.class);

        assertNotNull(oc);
        assertEquals(NoopObjectCache.class, oc.getClass());
    }
}
