/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Empty;

public final class EmptyDeserializer extends StdDeserializer<Empty> {
    private static final long serialVersionUID = 1L;

    public EmptyDeserializer() {
        super(Empty.class);
    }

    @Override
    @SuppressWarnings("checkstyle:ParameterName")
    public Empty deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        // FIXME: read according to
        // https://tools.ietf.org/html/rfc7951#section-6.9
        return Empty.value();
    }
}
