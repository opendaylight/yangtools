/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;

/**
  * A codec, which knows what the native representation for a particular data type is. It knows how to convert a native
  * value to and from string representation based on some additional input or output context.
  *
  * @author Robert Varga
  *
  * @param <T> Data value type
  * @param <I> Input context type
  * @param <O> Output context type
  */
@Beta
public interface TypeAwareCodec<T, I, O> {
    /**
     * Return the data type class.
     *
     * @return Data type class
     */
    Class<T> getDataType();

    /**
     * Parse a String representation into its native format.
     *
     * @param ctx Parse context
     * @param str String representation
     * @return Value in native format
     * @throws IllegalArgumentException if the value does not parse or pass type validation
     */
    T parseValue(I ctx, String str) throws Exception;

    /**
     * Serialize specified value with specified JsonWriter.
     *
     * @param ctx Write context
     * @param value Value in native format
     * @throws Exception if the write fails
     */
    void writeValue(O ctx, T value) throws Exception;
}
