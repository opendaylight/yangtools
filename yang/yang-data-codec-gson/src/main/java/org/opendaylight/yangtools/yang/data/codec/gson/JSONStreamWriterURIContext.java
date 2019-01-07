/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Abstract class tracking a virtual level of {@link JSONNormalizedNodeStreamWriter}
 * recursion. It only tracks the namespace associated with this node.
 */
abstract class JSONStreamWriterURIContext extends JSONStreamWriterContext {
    private final URI namespace;

    protected JSONStreamWriterURIContext(final JSONStreamWriterContext parent, final URI namespace) {
        super(parent, false);
        this.namespace = namespace;
    }

    @Override
    protected final URI getNamespace() {
        return namespace;
    }

    @Override
    protected void emitStart(final SchemaContext schema, final JsonWriter writer) throws IOException {
        // No-op
    }
}