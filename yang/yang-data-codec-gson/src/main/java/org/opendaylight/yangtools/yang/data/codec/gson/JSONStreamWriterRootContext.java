/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.net.URI;

/**
 * The root node of a particular {@link JSONNormalizedNodeStreamWriter} instance.
 * It holds the base namespace and can never be removed from the stack.
 */
abstract class JSONStreamWriterRootContext extends JSONStreamWriterURIContext {
    JSONStreamWriterRootContext(final URI namespace) {
        super(null, namespace);
    }
}
