/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class Bug4051Test {

    private static SchemaContext context;
    private static QNameModule foo;
    private static QName l2;
    private static QName interconnection;
    private static QName bridgeBased;
    private static QName bridgeDomain;

    @BeforeClass
    public static void init() throws URISyntaxException, IOException, YangSyntaxErrorException, ParseException {
        initSchemaContext();
        initQnames();
    }

    private static void initQnames() throws URISyntaxException, ParseException {
        foo = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat().parse("2015-09-10"));

        l2 = QName.create(foo, "l2");
        interconnection = QName.create(foo, "interconnection");
        bridgeBased = QName.create(foo, "bridge-based");
        bridgeDomain = QName.create(foo, "bridge-domain");
    }

    private static void initSchemaContext() throws URISyntaxException, IOException, YangSyntaxErrorException {
        final File resourceFile = new File(Bug4051Test.class.getResource("/bug-4051/foo.yang").toURI());

        final File resourceDir = resourceFile.getParentFile();

        final YangParserImpl parser = YangParserImpl.getInstance();
        context = parser.parseFile(resourceFile, resourceDir);
    }

    @Test
    public void bug4051Test() throws XMLStreamException, FactoryConfigurationError {
        SchemaPath leafrefPath = SchemaPath.create(true, l2, interconnection, bridgeBased, bridgeDomain);
        SchemaNode foundNode = SchemaContextUtil.findDataSchemaNode(context, leafrefPath);
        assertNotNull(foundNode);
        assertTrue(foundNode instanceof LeafSchemaNode);
        LeafSchemaNode bridgeDomainLeafref = (LeafSchemaNode) foundNode;

        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
        XmlStreamUtils xmlUtils = XmlStreamUtils.create(XmlUtils.DEFAULT_XML_CODEC_PROVIDER, context);

        HashSet<String> value = new LinkedHashSet<>();
        value.add("one");
        value.add("two");
        value.add("three");
        xmlUtils.writeValue(xmlWriter, bridgeDomainLeafref, value);

        String output = stringWriter.toString();
        assertEquals("one two three", output);
    }
}
