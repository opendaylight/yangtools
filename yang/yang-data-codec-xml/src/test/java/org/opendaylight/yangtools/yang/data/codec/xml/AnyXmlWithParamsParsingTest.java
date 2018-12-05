/*
 * Copyright (c) 2018 FRINX Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;

public class AnyXmlWithParamsParsingTest {

    private static final InputStream EDIT_CONFIG = XmlToNormalizedNodesTest.class.getResourceAsStream(
            "/anyxml-support/params/edit.xml");

    private static final SchemaContext SCHEMA = YangParserTestUtils.parseYangResourceDirectory(
            "/anyxml-support/params/");

    private static final SchemaNode SCHEMA_NODE = SCHEMA.getOperations().stream()
            .filter(o -> o.getQName().getLocalName().equals("edit-config"))
            .findFirst()
            .map(OperationDefinition::getInput)
            .get();

    @Test
    public void testAnyXmlWithParams() throws Exception {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(EDIT_CONFIG);

        final NormalizedNodeResult resultHolder = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        final XmlParserStream xmlParser = XmlParserStream.create(writer, SCHEMA, SCHEMA_NODE);
        xmlParser.traverse(new DOMSource(doc.getDocumentElement()));
        final NormalizedNode<?, ?> parsed = resultHolder.getResult();

        final DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> editCfg = ((ContainerNode) parsed)
                .getChild(getNodeId(parsed, "edit-content")).get();

        final DOMSource anyXmlParsedDom = ((AnyXmlNode) ((ChoiceNode) editCfg)
                .getChild(getNodeId(parsed, "config")).get())
                .getValue();

        assertNotNull(anyXmlParsedDom);
        final String anyXmlParsedDomString = toStringDom(anyXmlParsedDom);

        assertThat(anyXmlParsedDomString, containsString(
                "active xmpref:prefixed2=\"attribute2\""));
        assertThat(anyXmlParsedDomString, containsString(
                "interface-name xmpref:prefixed3=\"attribute3\""));
        assertThat(anyXmlParsedDomString, containsString(
                "interface-configuration xmlns:xmpref=\"xml:namespace:prefix\" simple=\"attribute\""));
        assertThat(anyXmlParsedDomString, containsString(
                "interface-configurations xmlns=\"http://cisco.com/ns/yang/Cisco-IOS-XR-ifmgr-cfg\""));
    }

    private YangInstanceIdentifier.NodeIdentifier getNodeId(final NormalizedNode<?, ?> parsed, final String localName) {
        return new YangInstanceIdentifier.NodeIdentifier(QName.create(parsed.getNodeType(), localName));
    }

    private static String toStringDom(final DOMSource source) {
        try {
            final StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, new StreamResult(sw));
            return sw.toString();
        } catch (final TransformerException ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
}