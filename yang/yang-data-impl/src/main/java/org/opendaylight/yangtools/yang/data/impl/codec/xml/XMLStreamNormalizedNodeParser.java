/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XMLStreamNormalizedNodeParser {
    /**
     * String->URI cache is global, shared across instances
     */
    private static final LoadingCache<String, QNameModule> URI_CACHE =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, QNameModule>() {
        @Override
        public QNameModule load(final String key) throws URISyntaxException {
            return QNameModule.create(new URI(key), null);
        }
    });
    private static final Logger LOG = LoggerFactory.getLogger(XMLStreamNormalizedNodeParser.class);

    private final NormalizedNodeStreamWriter writer;
    private final SchemaContext context;

    private XMLStreamNormalizedNodeParser(final NormalizedNodeStreamWriter writer, final SchemaContext context) {
        this.writer = Preconditions.checkNotNull(writer);
        this.context = Preconditions.checkNotNull(context);
    }

    public XMLStreamNormalizedNodeParser create(final NormalizedNodeStreamWriter writer, final SchemaContext context) {
        return new XMLStreamNormalizedNodeParser(writer, context);
    }

    /**
     * Parse the XML stream produced by an XMLStreamReader. The stream reader has to
     * be configured to have <code>javax.xml.stream.isCoalescing</code> enabled.
     *
     * @param reader XML stream to parse
     * @return Reference to this parser for fluent use.
     * @throws IOException when the underlying NormalizedNodeStreamWriter fails
     * @throws XMLStreamException if an error in the XML stream is encountered
     * @throws IllegalArgumentException if the XML reader is not coalescing
     */
    public XMLStreamNormalizedNodeParser parse(final XMLStreamReader reader) throws IOException, XMLStreamException {
        Object coalescing = reader.getProperty("javax.xml.stream.isCoalescing");
        Preconditions.checkArgument(Boolean.TRUE.equals(coalescing), "XML stream reader has to be coalescing");



        return this;
    }
}
