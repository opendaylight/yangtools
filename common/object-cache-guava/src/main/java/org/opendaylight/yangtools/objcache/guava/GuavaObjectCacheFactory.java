/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.guava;

import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.spi.IObjectCacheFactory;

public final class GuavaObjectCacheFactory implements IObjectCacheFactory {
	private static final GuavaObjectCacheFactory INSTANCE = new GuavaObjectCacheFactory();
	private final ObjectCache cache;

	private GuavaObjectCacheFactory() {
		// FIXME
		this.cache = new GuavaObjectCache(null);
	}

	@Override
	public ObjectCache getObjectCache(final Class<?> objClass) {
		return cache;
	}

	public static GuavaObjectCacheFactory getInstance() {
		return INSTANCE;
	}
}
