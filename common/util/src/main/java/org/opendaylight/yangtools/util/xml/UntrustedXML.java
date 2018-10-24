/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import com.google.common.annotations.Beta;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.function.Supplier;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Set of utility methods for instantiating parser that deal with untrusted XML sources.
 *
 * @author Robert Varga
 */
@Beta
public final class UntrustedXML {
    private static final DocumentBuilderFactory DBF;

    static {
        final DocumentBuilderFactory f = getLimited(DocumentBuilderFactory::newInstance);
        f.setCoalescing(true);
        f.setExpandEntityReferences(false);
        f.setIgnoringElementContentWhitespace(true);
        f.setIgnoringComments(true);
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (final ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
        DBF = f;
    }

    private static final SAXParserFactory SPF;

    static {
        final SAXParserFactory f = getLimited(SAXParserFactory::newInstance);
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (final SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }

        SPF = f;
    }

    private static final XMLInputFactory XIF;

    static {
        final XMLInputFactory f = getLimited(XMLInputFactory::newInstance);
        f.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        f.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        f.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        XIF = f;
    }

    private UntrustedXML() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new {@link DocumentBuilder} for dealing with untrusted XML data. This method is equivalent to
     * {@link DocumentBuilderFactory#newDocumentBuilder()}, except it does not throw a checked exception.
     *
     * @return A new DocumentBuilder
     * @throws UnsupportedOperationException if the runtime fails to instantiate a good enough builder
     */
    public static @NonNull DocumentBuilder newDocumentBuilder() {
        try {
            return DBF.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException("Failed to instantiate a DocumentBuilder", e);
        }
    }

    /**
     * Create a new {@link SAXParser} for dealing with untrusted XML data. This method is equivalent to
     * {@link SAXParserFactory#newSAXParser()}, except it does not throw a checked exception.
     *
     * @return A new SAXParser
     * @throws UnsupportedOperationException if the runtime fails to instantiate a good enough builder
     */
    public static @NonNull SAXParser newSAXParser() {
        try {
            return SPF.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new UnsupportedOperationException("Failed to instantiate a SAXParser", e);
        }
    }

    /**
     * Create a new {@link XMLStreamReader} for dealing with untrusted XML data. This method is equivalent to
     * {@link XMLInputFactory#createXMLStreamReader(InputStream)}.
     *
     * @return A new XMLStreamReader
     * @throws XMLStreamException when the underlying factory throws it
     */
    public static @NonNull XMLStreamReader createXMLStreamReader(final InputStream stream) throws XMLStreamException {
        return XIF.createXMLStreamReader(stream);
    }

    /**
     * Create a new {@link XMLStreamReader} for dealing with untrusted XML data. This method is equivalent to
     * {@link XMLInputFactory#createXMLStreamReader(InputStream, String)}, except it takes an explict charset argument.
     *
     * @return A new XMLStreamReader
     * @throws XMLStreamException when the underlying factory throws it
     */
    public static @NonNull XMLStreamReader createXMLStreamReader(final InputStream stream, final Charset charset)
            throws XMLStreamException {
        return XIF.createXMLStreamReader(stream, charset.name());
    }

    /**
     * Create a new {@link XMLStreamReader} for dealing with untrusted XML data. This method is equivalent to
     * {@link XMLInputFactory#createXMLStreamReader(Reader)}.
     *
     * @return A new XMLStreamReader
     * @throws XMLStreamException when the underlying factory throws it
     */
    public static @NonNull XMLStreamReader createXMLStreamReader(final Reader reader) throws XMLStreamException {
        return XIF.createXMLStreamReader(reader);
    }

    private static <T> T getLimited(final @NonNull Supplier<T> supplier) {
        final ClassLoader loader = UntrustedXML.class.getClassLoader();
        return loader == null ? supplier.get() : ClassLoaderUtils.getWithClassLoader(loader, supplier);
    }
}
