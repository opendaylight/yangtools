/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.Beta;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.codehaus.stax2.ri.dom.DOMWrappingReader;

@Beta
public final class DOMSourceXMLStreamReader extends DOMWrappingReader {
    private DOMSourceXMLStreamReader(final DOMSource src) throws XMLStreamException {
        super(src, true, true);
    }

    public static XMLStreamReader wrap(final DOMSource src) throws XMLStreamException {
        return new DOMSourceXMLStreamReader(src);
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

    @Override
    protected void throwStreamException(final String msg, final Location loc) throws XMLStreamException {
        throw new XMLStreamException(msg, loc);
    }
}
