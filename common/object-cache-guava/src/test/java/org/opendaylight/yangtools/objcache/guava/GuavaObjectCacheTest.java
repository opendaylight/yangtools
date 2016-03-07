/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.guava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.ObjectCacheFactory;

public class GuavaObjectCacheTest {
	private ObjectCache cache;

	@Before
	public void setUp() {
		cache = ObjectCacheFactory.getObjectCache(String.class);
	}

	@Test
	public void testCorrectWiring() {
		assertEquals(GuavaObjectCache.class, cache.getClass());
	}

	@Test
	public void testInitialReference() {
		final String s1 = "abcd";
		final String s2 = cache.getReference(s1);
		assertSame(s1, s2);
	}

	@Test
	// This test is based on using different references
	@SuppressWarnings("RedundantStringConstructorCall")
	public void testMultipleReferences() {
		final String s1 = "abcd";
		final String s2 = new String(s1);

		// Preliminary check
		assertEquals(s1, s2);
		assertNotSame(s1, s2);

		assertSame(s1, cache.getReference(s1));
		assertSame(s1, cache.getReference(s2));
		assertNotSame(s2, cache.getReference(s2));
	}

}
