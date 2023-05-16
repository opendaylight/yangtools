/*
 * Copyright (c) 2018 FRINX Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DOMSourceAnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;

public class AnyXmlWithParamsParsingTest {
    private static final InputStream EDIT_CONFIG = XmlToNormalizedNodesTest.class.getResourceAsStream(
            "/anyxml-support/params/edit.xml");

    private static final QNameModule IETF_NETCONF = QNameModule.create(
        XMLNamespace.of("urn:ietf:params:xml:ns:netconf:base:1.0"), Revision.of("2011-06-01"));

    @Test
    public void testAnyXmlWithParams() throws Exception {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResourceDirectory("/anyxml-support/params/");

        final Document doc = UntrustedXML.newDocumentBuilder().parse(EDIT_CONFIG);

        final var resultHolder = new NormalizationResultHolder();
        final var writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);

        final XmlParserStream xmlParser = XmlParserStream.create(writer, SchemaInferenceStack.of(context,
            Absolute.of(QName.create(IETF_NETCONF, "edit-config"), YangConstants.operationInputQName(IETF_NETCONF)))
            .toInference());
        xmlParser.traverse(new DOMSource(doc.getDocumentElement()));
        final NormalizedNode parsed = resultHolder.getResult().data();

        final DataContainerChild editCfg = ((ContainerNode) parsed).childByArg(getNodeId(parsed, "edit-content"));
        final DOMSource anyXmlParsedDom = ((DOMSourceAnyxmlNode) ((ChoiceNode) editCfg)
                .childByArg(getNodeId(parsed, "config"))).body();

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

    private static NodeIdentifier getNodeId(final NormalizedNode parsed, final String localName) {
        return new NodeIdentifier(QName.create(parsed.getIdentifier().getNodeType(), localName));
    }

    private static String toStringDom(final DOMSource source) {
        try {
            final StringWriter sw = new StringWriter();
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
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