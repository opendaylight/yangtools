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
import java.time.format.DateTimeParseException;
import org.opendaylight.yangtools.yang.common.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RevisionDeserializer extends StdScalarDeserializer<Revision> {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionDeserializer.class);
    private static final long serialVersionUID = 1L;

    public RevisionDeserializer() {
        super(Revision.class);
    }

    @Override
    public Revision deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final String str = _parseString(p, ctxt);
        try {
            return Revision.of(str);
        } catch (DateTimeParseException e) {
            LOG.debug("Swallowed exception", e);
            throw MismatchedInputException.from(p, Revision.class, e.getMessage());
        }
    }
}
