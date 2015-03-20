/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class XmlDocumentUtils {

    private XmlDocumentUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static final QName OPERATION_ATTRIBUTE_QNAME = QName.create(SchemaContext.NAME, "operation");
    private static final Logger LOG = LoggerFactory.getLogger(XmlDocumentUtils.class);

    public static Document getDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try {
            DocumentBuilder bob = dbf.newDocumentBuilder();
            doc = bob.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    private static final Element createElementFor(final Document doc, final QName qname, final Object obj) {
        final Element ret;
        if (qname.getNamespace() != null) {
            ret = doc.createElementNS(qname.getNamespace().toString(), qname.getLocalName());
        } else {
            ret = doc.createElementNS(null, qname.getLocalName());
        }

        if (obj instanceof AttributesContainer) {
            final Map<QName, String> attrs = ((AttributesContainer)obj).getAttributes();

            if (attrs != null) {
                for (Entry<QName, String> attribute : attrs.entrySet()) {
                    ret.setAttributeNS(attribute.getKey().getNamespace().toString(), attribute.getKey().getLocalName(),
                            attribute.getValue());
                }
            }
        }

        return ret;
    }

    public static Element createElementFor(final Document doc, final NormalizedNode<?, ?> data) {
        return createElementFor(doc, data.getNodeType(), data);
    }

    public static QName qNameFromElement(final Element xmlElement) {
        String namespace = xmlElement.getNamespaceURI();
        String localName = xmlElement.getLocalName();
        return QName.create(namespace != null ? URI.create(namespace) : null, null, localName);
    }



    public static Optional<ModifyAction> getModifyOperationFromAttributes(final Element xmlElement) {
        Attr attributeNodeNS = xmlElement.getAttributeNodeNS(OPERATION_ATTRIBUTE_QNAME.getNamespace().toString(), OPERATION_ATTRIBUTE_QNAME.getLocalName());
        if(attributeNodeNS == null) {
            return Optional.absent();
        }

        ModifyAction action = ModifyAction.fromXmlValue(attributeNodeNS.getValue());
        Preconditions.checkArgument(action.isOnElementPermitted(), "Unexpected operation %s on %s", action, xmlElement);

        return Optional.of(action);
    }


    public static final Optional<DataSchemaNode> findFirstSchema(final QName qname, final Iterable<DataSchemaNode> dataSchemaNode) {
        if (dataSchemaNode != null && qname != null) {
            for (DataSchemaNode dsn : dataSchemaNode) {
                if (qname.isEqualWithoutRevision(dsn.getQName())) {
                    return Optional.<DataSchemaNode> of(dsn);
                } else if (dsn instanceof ChoiceSchemaNode) {
                    for (ChoiceCaseNode choiceCase : ((ChoiceSchemaNode) dsn).getCases()) {
                        Optional<DataSchemaNode> foundDsn = findFirstSchema(qname, choiceCase.getChildNodes());
                        if (foundDsn != null && foundDsn.isPresent()) {
                            return foundDsn;
                        }
                    }
                }
            }
        }
        return Optional.absent();
    }

    public static final XmlCodecProvider defaultValueCodecProvider() {
        return XmlUtils.DEFAULT_XML_CODEC_PROVIDER;
    }
}
