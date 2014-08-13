/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

public final class InstanceIdentifierForXmlCodec {
    private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    private InstanceIdentifierForXmlCodec() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static YangInstanceIdentifier deserialize(final Element element, final SchemaContext schemaContext) {
        Preconditions.checkNotNull(element, "Value of element for deserialization can't be null");
        Preconditions.checkNotNull(schemaContext,
                "Schema context for deserialization of instance identifier type can't be null");

        final String valueTrimmed = element.getTextContent().trim();
        final Iterator<String> xPathParts = SLASH_SPLITTER.split(valueTrimmed).iterator();

        // must be at least "/pr:node"
        if (!xPathParts.hasNext() || !xPathParts.next().isEmpty() || !xPathParts.hasNext()) {
            return null;
        }

        List<PathArgument> result = new ArrayList<>();
        while (xPathParts.hasNext()) {
            String xPathPartTrimmed = xPathParts.next().trim();

            PathArgument pathArgument = toPathArgument(xPathPartTrimmed, element, schemaContext);
            if (pathArgument != null) {
                result.add(pathArgument);
            }
        }
        return YangInstanceIdentifier.create(result);
    }

    public static Element serialize(final YangInstanceIdentifier id, final Element element) {
        Preconditions.checkNotNull(id, "Variable should contain instance of instance identifier and can't be null");
        Preconditions.checkNotNull(element, "DOM element can't be null");

        final RandomPrefix prefixes = new RandomPrefix();
        final String str = XmlUtils.encodeIdentifier(prefixes, id);

        for (Entry<URI, String> e: prefixes.getPrefixes()) {
            element.setAttribute("xmlns:" + e.getValue(), e.getKey().toString());
        }
        element.setTextContent(str);
        return element;
    }

    private static String getIdAndPrefixAsStr(final String pathPart) {
        int predicateStartIndex = pathPart.indexOf('[');
        return predicateStartIndex == -1 ? pathPart : pathPart.substring(0, predicateStartIndex);
    }

    private static PathArgument toPathArgument(final String xPathArgument, final Element element, final SchemaContext schemaContext) {
        final QName mainQName = toIdentity(xPathArgument, element, schemaContext);

        // predicates
        final Matcher matcher = PREDICATE_PATTERN.matcher(xPathArgument);
        final Map<QName, Object> predicates = new HashMap<>();
        QName currentQName = mainQName;

        while (matcher.find()) {
            final String predicateStr = matcher.group(1).trim();
            final int indexOfEqualityMark = predicateStr.indexOf('=');
            if (indexOfEqualityMark != -1) {
                final String predicateValue = toPredicateValue(predicateStr.substring(indexOfEqualityMark + 1));
                if (predicateValue == null) {
                    return null;
                }

                if (predicateStr.charAt(0) != '.') {
                    // target is not a leaf-list
                    currentQName = toIdentity(predicateStr.substring(0, indexOfEqualityMark), element, schemaContext);
                    if (currentQName == null) {
                        return null;
                    }
                }
                predicates.put(currentQName, predicateValue);
            }
        }

        if (predicates.isEmpty()) {
            return new YangInstanceIdentifier.NodeIdentifier(mainQName);
        } else {
            return new YangInstanceIdentifier.NodeIdentifierWithPredicates(mainQName, predicates);
        }

    }

    public static QName toIdentity(final String xPathArgument, final Element element, final SchemaContext schemaContext) {
        final String xPathPartTrimmed = getIdAndPrefixAsStr(xPathArgument).trim();
        final Iterator<String> it = COLON_SPLITTER.split(xPathPartTrimmed).iterator();

        // Empty string
        if (!it.hasNext()) {
            return null;
        }

        final String prefix = it.next().trim();
        if (prefix.isEmpty()) {
            return null;
        }

        // it is not "prefix:value"
        if (!it.hasNext()) {
            return null;
        }

        final String identifier = it.next().trim();
        if (identifier.isEmpty()) {
            return null;
        }

        URI namespace = null;
        String namespaceStr = null;
        try {
            namespaceStr = element.lookupNamespaceURI(prefix);
            namespace = new URI(namespaceStr);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("It wasn't possible to convert " + namespaceStr + " to URI object.");
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("It wasn't possible to get namespace for prefix " + prefix);
        }

        final Module module = schemaContext.findModuleByNamespaceAndRevision(namespace, null);
        Preconditions.checkNotNull(module, "Unknown module: %s, cannot parse identity %s", namespace, xPathArgument);
        return QName.create(module.getQNameModule(), identifier);
    }

    private static String trimIfEndIs(final String str, final char end) {
        final int l = str.length() - 1;
        if (str.charAt(l) != end) {
            return null;
        }

        return str.substring(1, l);
    }

    private static String toPredicateValue(final String predicatedValue) {
        final String predicatedValueTrimmed = predicatedValue.trim();
        if (predicatedValue.isEmpty()) {
            return null;
        }

        switch (predicatedValueTrimmed.charAt(0)) {
        case '"':
            return trimIfEndIs(predicatedValueTrimmed, '"');
        case '\'':
            return trimIfEndIs(predicatedValueTrimmed, '\'');
        default:
            return null;
        }
    }
}
