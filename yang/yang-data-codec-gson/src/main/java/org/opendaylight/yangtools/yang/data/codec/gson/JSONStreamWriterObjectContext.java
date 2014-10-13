/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Writer;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A recursion level of {@link JSONNormalizedNodeStreamWriter}, which represents
 * a JSON object which does not have to be prefixed with its identifier -- such
 * as when it is in a containing list.
 */
class JSONStreamWriterObjectContext extends JSONStreamWriterQNameContext {
    protected JSONStreamWriterObjectContext(final JSONStreamWriterContext parent, final PathArgument arg, final boolean mandatory) {
        super(Preconditions.checkNotNull(parent), arg.getNodeType(), mandatory);
    }

    @Override
    protected void emitStart(final SchemaContext schema, final Writer writer) throws IOException {
        writer.append('{');
    }

    @Override
    protected void emitEnd(final Writer writer, final String indent) throws IOException {
        super.emitEnd(writer, indent);
        writer.append('}');
    }
}