/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

/**
 * A simplistic factory, which does not perform any codec caching. Minimizes resident memory footprint at the expense
 * of creating short-lived objects.
 *
 * @author Robert Varga
 */
@ThreadSafe
final class SimpleJSONCodecFactory extends JSONCodecFactory {
    SimpleJSONCodecFactory(final SchemaContext context) {
        super(context);
    }

    @Override
    JSONCodec<?> codecFor(final TypedSchemaNode schema) {
        return createCodec(schema, schema.getType());
    }
}
