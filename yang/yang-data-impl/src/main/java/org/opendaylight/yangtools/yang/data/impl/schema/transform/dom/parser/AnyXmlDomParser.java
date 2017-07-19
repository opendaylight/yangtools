/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import com.google.common.base.Preconditions;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.AnyXmlNodeBaseParser;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.w3c.dom.Element;

/**
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public class AnyXmlDomParser extends AnyXmlNodeBaseParser<Element> {

    @Override
    protected DOMSource parseAnyXml(final Element element, final AnyXmlSchemaNode schema) {
        final QName qName = schema.getQName();
        Preconditions.checkArgument(element.getNodeName().equals(qName.getLocalName()));
        Preconditions.checkArgument(element.getNamespaceURI().equals(qName.getNamespace().toString()));
        return new DOMSource(element);
    }
}
