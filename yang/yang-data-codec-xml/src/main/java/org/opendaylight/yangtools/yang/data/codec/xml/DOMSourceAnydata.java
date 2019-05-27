/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.data.util.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.NormalizedAnydata;

@NonNullByDefault
final class DOMSourceAnydata implements NormalizableAnydata {
    private final DOMSource source;

    DOMSourceAnydata(final DOMSource source) {
        this.source = requireNonNull(source);
    }

    DOMSource getSource() {
        return source;
    }

    @Override
    public NormalizedAnydata normalizeTo(final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) {
        // TODO Auto-generated method stub
        return null;
    }
}
