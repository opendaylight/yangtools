/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

abstract class AbstractXmlTest {
    private static final TransformerFactory TF = TransformerFactory.newInstance();

    static final Document loadDocument(final String resourcePath) {
        return requireNonNull(readXmlToDocument(Bug5446Test.class.getResourceAsStream(resourcePath)));
    }

    static final Document readXmlToDocument(final InputStream xmlContent) {
        final Document doc;
        try {
            doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        } catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
        doc.getDocumentElement().normalize();
        return doc;
    }

    static final String toString(final Node xml) {
        final Transformer transformer;
        try {
            transformer = TF.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new AssertionError(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        final var result = new StreamResult(new StringWriter());
        try {
            transformer.transform(new DOMSource(xml), result);
        } catch (TransformerException e) {
            throw new AssertionError(e);
        }
        return result.getWriter().toString();
    }
}
