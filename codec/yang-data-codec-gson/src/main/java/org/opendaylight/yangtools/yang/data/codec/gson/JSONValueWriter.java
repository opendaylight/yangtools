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

    void writeBoolean(boolean value) throws IOException;

    void writeEmpty() throws IOException;

    void writeNumber(Number value) throws IOException;

    void writeString(String value) throws IOException;
}
