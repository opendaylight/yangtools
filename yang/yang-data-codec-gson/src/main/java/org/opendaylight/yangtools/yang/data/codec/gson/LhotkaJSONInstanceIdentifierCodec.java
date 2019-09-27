/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class LhotkaJSONInstanceIdentifierCodec extends AbstractJSONInstanceIdentifierCodec {
    private final LhotkaJSONCodecFactory codecFactory;

    LhotkaJSONInstanceIdentifierCodec(final SchemaContext context, final LhotkaJSONCodecFactory jsonCodecFactory) {
        this.context = requireNonNull(context);
        this.dataContextTree = DataSchemaContextTree.from(context);
        this.codecFactory = requireNonNull(jsonCodecFactory);
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        checkArgument(schemaNode instanceof LeafSchemaNode, "schemaNode must be of type LeafSchemaNode");
        final JSONCodec<?> objectJSONCodec = codecFactory.codecFor((LeafSchemaNode) schemaNode);
        return objectJSONCodec.parseValue(null, value);
    }
}
