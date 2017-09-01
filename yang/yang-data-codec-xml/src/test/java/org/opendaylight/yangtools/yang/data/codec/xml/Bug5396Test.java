/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Optional;
import java.io.InputStream;
import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug5396Test {

    private QNameModule fooModuleQName;
    private SchemaContext schemaContext;

    @Before
    public void setUp() throws Exception {
        fooModuleQName = QNameModule.create(new URI("foo"), SimpleDateFormatUtil.getRevisionFormat().parse(
                    "2016-03-22"));
        schemaContext = YangParserTestUtils.parseYangResource("/bug5396/yang/foo.yang");
    }

    @Test
    public void test() throws Exception {
        testInputXML("/bug5396/xml/foo.xml", "dp1o34");
        testInputXML("/bug5396/xml/foo2.xml", "dp0s3f9");
        testInputXML("/bug5396/xml/foo3.xml", "dp09P1p2s3");
        testInputXML("/bug5396/xml/foo4.xml", "dp0p3p1");
        testInputXML("/bug5396/xml/foo5.xml", "dp0s3");

        try {
            testInputXML("/bug5396/xml/invalid-foo.xml", null);
            fail("Test should fail due to invalid input string");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Invalid value \"dp09P1p2s1234\" for union type."));
        }
    }

    private void testInputXML(final String xmlPath, final String expectedValue) throws Exception {
        final InputStream resourceAsStream = XmlToNormalizedNodesTest.class.getResourceAsStream(xmlPath);
        final Module fooModule = schemaContext.getModules().iterator().next();
        final ContainerSchemaNode rootCont = (ContainerSchemaNode) fooModule.getDataChildByName(
                QName.create(fooModule.getQNameModule(), "root"));
        assertNotNull(rootCont);

        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();

        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, schemaContext, rootCont);
        xmlParser.parse(reader);

        assertNotNull(result.getResult());
        assertTrue(result.getResult() instanceof ContainerNode);
        final ContainerNode rootContainer = (ContainerNode) result.getResult();

        Optional<DataContainerChild<? extends PathArgument, ?>> myLeaf = rootContainer.getChild(new NodeIdentifier(
                QName.create(fooModuleQName, "my-leaf")));
        assertTrue(myLeaf.orNull() instanceof LeafNode);

        assertEquals(expectedValue, myLeaf.get().getValue());
    }
}
