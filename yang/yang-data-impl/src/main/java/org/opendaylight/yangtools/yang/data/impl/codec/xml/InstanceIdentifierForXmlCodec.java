/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.net.URI;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

/**
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public final class InstanceIdentifierForXmlCodec {
    private InstanceIdentifierForXmlCodec() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static YangInstanceIdentifier deserialize(final Element element, final SchemaContext schemaContext) {
        final ElementInstanceIdentifierParser codec = new ElementInstanceIdentifierParser(schemaContext, element);
        return codec.deserialize(element.getTextContent().trim());
    }

    public static Element serialize(final YangInstanceIdentifier id, final Element element, final SchemaContext context) {
        final RandomPrefixInstanceIdentifierSerializer codec = new RandomPrefixInstanceIdentifierSerializer(context);
        final String str = codec.serialize(id);

        for (Entry<URI, String> e : codec.getPrefixes()) {
            element.setAttribute("xmlns:" + e.getValue(), e.getKey().toString());
        }
        element.setTextContent(str);
        return element;
    }

    private static String getIdAndPrefixAsStr(final String pathPart) {
        int predicateStartIndex = pathPart.indexOf('[');
        return predicateStartIndex == -1 ? pathPart : pathPart.substring(0, predicateStartIndex);
    }

    public static QName toIdentity(final String xPathArgument, final Element element, final SchemaContext schemaContext) {
        final ElementIdentityrefParser codec = new ElementIdentityrefParser(schemaContext, element);
        return codec.deserialize(getIdAndPrefixAsStr(xPathArgument).trim());
    }

}
