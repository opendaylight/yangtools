/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.data;

/**
 * A retrieval strategy which places an upper bound on the depth of the tree
 * returned by the query.
 */
public interface LimitedDepthRetrievalStrategy extends RetrievalStrategy {
	/**
	 * @return maximum three depth
	 */
    int getDepthLimit();
}
