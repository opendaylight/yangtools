/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.w3c.dom.Document;

public class XmlStreamUtilsTest {

    public static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newFactory();

    @Test
    public void testWriteAttribute() throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final XMLStreamWriter writer =  XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
        writer.writeStartElement("element");

        QName name = getAttrQName("namespace", "2012-12-12", "attr", Optional.of("prefix"));
        final Map.Entry<QName, String> attributeEntry = new AbstractMap.SimpleEntry<>(name, "value");

        name = getAttrQName("namespace2", "2012-12-12", "attr", Optional.<String>absent());
        final Map.Entry<QName, String> attributeEntryNoPrefix = new AbstractMap.SimpleEntry<>(name, "value");

        final RandomPrefix randomPrefix = new RandomPrefix();
        XmlStreamUtils.writeAttribute(writer, attributeEntry, randomPrefix);
        XmlStreamUtils.writeAttribute(writer, attributeEntryNoPrefix, randomPrefix);

        writer.writeEndElement();
        writer.close();
        out.close();

        final String xmlAsString = new String(out.toByteArray());

        final Map<String, String> mappedPrefixes = mapPrefixed(randomPrefix.getPrefixes());
        assertEquals(2, mappedPrefixes.size());
        final String randomPrefixValue = mappedPrefixes.get("namespace2");

        final String expectedXmlAsString = "<element xmlns:prefix=\"namespace\" prefix:attr=\"value\" xmlns:" + randomPrefixValue + "=\"namespace2\" " + randomPrefixValue + ":attr=\"value\"></element>";

        XMLUnit.setIgnoreAttributeOrder(true);
        final Document control = XMLUnit.buildControlDocument(expectedXmlAsString);
        final Document test = XMLUnit.buildTestDocument(xmlAsString);
        final Diff diff = XMLUnit.compareXML(control, test);

        final boolean identical = diff.identical();
        assertTrue("Xml differs: " + diff.toString(), identical);
    }

    private Map<String, String> mapPrefixed(final Iterable<Map.Entry<URI, String>> prefixes) {
        final Map<String, String> mappedPrefixes = Maps.newHashMap();
        for (final Map.Entry<URI, String> prefix : prefixes) {
            mappedPrefixes.put(prefix.getKey().toString(), prefix.getValue());
        }
        return mappedPrefixes;
    }

    private QName getAttrQName(final String namespace, final String revision, final String localName, final Optional<String> prefix) {

        if(prefix.isPresent()) {
            final QName moduleQName = QName.create(namespace, revision, "module");
            final QNameModule module = QNameModule.create(moduleQName.getNamespace(), moduleQName.getRevision());
            return QName.create(module, prefix.get(), localName);
        } else {
            return QName.create(namespace, revision, localName);
        }
    }
}
