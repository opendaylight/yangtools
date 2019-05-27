/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gson.JsonElement;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractNormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;

@NonNullByDefault
final class JsonElementAnydata extends AbstractNormalizableAnydata {
    private final @Nullable JsonElement element;

    JsonElementAnydata(final @Nullable JsonElement element) {
        this.element = element;
    }

    @Override
    protected void writeTo(final NormalizedNodeStreamWriter streamWriter, final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("element", element);
    }
}
