/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.IOException;
import java.net.URI;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * The root node of a particular {@link JSONNormalizedNodeStreamWriter} instance.
 * It holds the base namespace and can never be removed from the stack.
 */
final class JSONStreamWriterRootContext extends JSONStreamWriterURIContext {
    JSONStreamWriterRootContext(final URI namespace) {
        super(null, namespace);
    }

    @Override
    protected void emitStart(final SchemaContext schema, final JSONWriter writer) throws IOException {
        writer.beginObject();
    }

    @Override
    protected void emitEnd(final JSONWriter writer) throws IOException {
        writer.endObject();
    }
}
