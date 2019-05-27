/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.JsonTreeReader;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractNormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@NonNullByDefault
final class JsonElementAnydata extends AbstractNormalizableAnydata {
    private final JSONCodecFactory codecs;
    private final @Nullable JsonElement element;

    JsonElementAnydata(final JSONCodecFactory codecs, final @Nullable JsonElement element) {
        this.codecs = requireNonNull(codecs);
        this.element = element;
    }

    @Nullable JsonElement getElement() {
        return element;
    }

    @Override
    protected void writeTo(final NormalizedNodeStreamWriter streamWriter, final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) throws IOException {
        // TODO: this is rather ugly
        final DataSchemaNode root = contextTree.getRoot().getDataSchemaNode();
        if (!(root instanceof SchemaContext)) {
            throw new IOException("Unexpected root context " + root);
        }

        final JSONCodecFactory factory = codecs.rebaseTo((SchemaContext) root);
        final JsonParserStream jsonParser;
        try {
            jsonParser = JsonParserStream.create(streamWriter, factory, verifyNotNull(contextNode.getDataSchemaNode()));
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to instantiate XML parser", e);
        }
        jsonParser.parse(new JsonTreeReader(element)).flush();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("element", element);
    }
}
