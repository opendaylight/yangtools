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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A recursion level of {@link JSONNormalizedNodeStreamWriter}, which represents
 * a JSON object which has to be prefixed with its identifier -- such as a
 * container.
 */
final class JSONStreamWriterNamedObjectContext extends JSONStreamWriterObjectContext {
    protected JSONStreamWriterNamedObjectContext(final JSONStreamWriterContext parent, final PathArgument arg,
            final boolean mandatory) {
        super(parent, arg, mandatory);
    }

    @Override
    protected void emitStart(final SchemaContext schema, final JsonWriter writer) throws IOException {
        writeMyJsonIdentifier(schema, writer, getQName());
        super.emitStart(schema, writer);
    }
}
