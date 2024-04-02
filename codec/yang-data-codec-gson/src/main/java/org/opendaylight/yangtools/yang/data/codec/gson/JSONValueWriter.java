/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A handler used to write out JSON-encoded values.
 */
@NonNullByDefault
public interface JSONValueWriter {
    /**
     * Write a {@code boolean} value, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7951#section-6.3">RFC7951, section 6.3</a>.
     *
     * @param value Value to write
     * @throws IOException when an IO error occurs
     */
    void writeBoolean(boolean value) throws IOException;

    /**
     * Write an {@code empty} value, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7951#section-6.9">RFC7951, section 6.9</a>.
     *
     * @throws IOException when an IO error occurs
     */
    void writeEmpty() throws IOException;

    /**
     * Write a numeric value, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7951#section-6.1">RFC7951, section 6.1</a>.
     *
     * @param value Value to write
     * @throws IOException when an IO error occurs
     */
    void writeNumber(Number value) throws IOException;

    /**
     * Write a string value, as per
     * <a href="https://www.rfc-editor.org/rfc/rfc7951#section-6.2">RFC7951, section 6.2</a> and other types which have
     * are represented as JSON strings.
     *
     * @param value Value to write
     * @throws IOException when an IO error occurs
     */
    void writeString(String value) throws IOException;
}
