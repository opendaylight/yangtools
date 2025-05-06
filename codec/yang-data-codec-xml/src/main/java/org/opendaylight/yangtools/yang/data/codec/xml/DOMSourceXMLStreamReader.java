/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.ri.dom.DOMWrappingReader;

/**
 * An {@link XMLStreamReader2} traversing over a {@link DOMSource}. The reader is always namespace-aware and coalescing.
 */
public final class DOMSourceXMLStreamReader extends DOMWrappingReader {
    /**
     * Default constructor.
     *
     * @param src backing {@link DOMSource}
     * @throws IllegalArgumentException when there is no root node
     * @throws XMLStreamException when the root node is not valid
     */
    public DOMSourceXMLStreamReader(final DOMSource src) throws XMLStreamException {
        super(src, true, true);
    }

    @Override
    public Object getProperty(final String name) {
        return null;
    }

    @Override
    public boolean isPropertySupported(final String name) {
        return false;
    }

    @Override
    public boolean setProperty(final String name, final Object value) {
        return false;
    }

    /**
     * {@inheritDoc}
     * @deprecated Do not call this method.
     */
    @Override
    @Deprecated(forRemoval = true)
    public void close() {
        // No-op
    }

    /**
     * {@inheritDoc}
     * @deprecated Do not call this method.
     */
    @Override
    @Deprecated(forRemoval = true)
    public void closeCompletely() {
        // No-op
    }

    @Override
    protected void throwStreamException(final String msg, final Location loc) throws XMLStreamException {
        throw new XMLStreamException(msg, loc);
    }
}
