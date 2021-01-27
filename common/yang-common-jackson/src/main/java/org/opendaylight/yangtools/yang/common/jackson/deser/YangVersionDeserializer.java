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
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.YangVersion;

public final class YangVersionDeserializer extends StdScalarDeserializer<YangVersion> {
    private static final long serialVersionUID = 1L;

    public YangVersionDeserializer() {
        super(YangVersion.class);
    }

    @Override
    @SuppressWarnings("checkstyle:ParameterName")
    public YangVersion deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final String str = _parseString(p, ctxt);
        return YangVersion.parse(str)
            .orElseThrow(() -> MismatchedInputException.from(p, YangVersion.class, "Invalid YANG version " + str));
    }
}
