/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.data;

/**
 * Collection of well-known retrieval strategies.
 */
public enum RetrievalStrategies implements RetrievalStrategy {
	/**
	 * Default retrieval strategy, letting the server decide what to do exactly.
	 */
	Default,
}
