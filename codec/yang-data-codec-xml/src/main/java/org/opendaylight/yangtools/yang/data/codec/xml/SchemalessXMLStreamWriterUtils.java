/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

final class SchemalessXMLStreamWriterUtils extends XMLStreamWriterUtils {
    static final SchemalessXMLStreamWriterUtils INSTANCE = new SchemalessXMLStreamWriterUtils();

    private SchemalessXMLStreamWriterUtils() {
        // Hidden on purpose
    }

    @Override
    String encode(final ValueWriter writer, final YangInstanceIdentifier value) {
        throw new UnsupportedOperationException("Schema context not present in " + this + ", cannot serialize "
            + value);
    }
}
