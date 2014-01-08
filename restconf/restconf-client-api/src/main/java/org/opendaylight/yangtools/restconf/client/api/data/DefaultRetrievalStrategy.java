/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.data;

/**
 * Default retrieval strategy, letting the server decide what to do exactly.
 */
public final class DefaultRetrievalStrategy implements RetrievalStrategy {
	private static final class Holder {
		private static final DefaultRetrievalStrategy INSTANCE = new DefaultRetrievalStrategy();
	}

	private DefaultRetrievalStrategy() {

	}

	public static DefaultRetrievalStrategy getInstance() {
		return Holder.INSTANCE;
	}
}
