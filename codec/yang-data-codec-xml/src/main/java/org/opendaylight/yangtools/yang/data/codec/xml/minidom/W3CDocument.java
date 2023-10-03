/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Document} backed by a {@link org.w3c.dom.Document}.
 */
@NonNullByDefault
record W3CDocument(org.w3c.dom.Document document) implements Document {
    public W3CDocument {
        requireNonNull(document);
    }

    @Override
    public Element element() {
        return W3CElement.of(document.getDocumentElement());
    }
}
