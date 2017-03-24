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

interface JSONCodec<T> {
    /**
     * Deserialize (parse) a String representation into its native format.
     *
     * @param value String representation
     * @return Value in native format
     * @throws IllegalArgumentException if the value does not parse or pass type validation
     */
    T deserializeString(String value);

    /**
     * Serialize specified value with specified JsonWriter.
     *
     * @param writer JsonWriter
     * @param value Value in native format
     * @throws IOException if the write fails
     */
    void serializeToWriter(JsonWriter writer, T value) throws IOException;

    /**
     * Return the internal representation class.
     *
     * @return Data representation class.
     */
    Class<T> getDataClass();
}
