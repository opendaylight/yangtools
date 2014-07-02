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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.w3c.dom.Element;

public final class InstanceIdentifierForXmlCodec {
    private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final String SQUOTE = "'";
    private static final String DQUOTE = "\"";

    private InstanceIdentifierForXmlCodec() {

    }

    public static InstanceIdentifier deserialize(final Element element, final SchemaContext schemaContext) {
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
        return InstanceIdentifier.create(result);
    }

    public static Element serialize(final InstanceIdentifier data, final Element element) {
        Preconditions.checkNotNull(data, "Variable should contain instance of instance identifier and can't be null");
        Preconditions.checkNotNull(element, "DOM element can't be null");
        Map<String, String> prefixes = new HashMap<>();
        StringBuilder textContent = new StringBuilder();
        for (PathArgument pathArgument : data.getPathArguments()) {
            textContent.append('/');
            writeIdentifierWithNamespacePrefix(element, textContent, pathArgument.getNodeType(), prefixes);
            if (pathArgument instanceof NodeIdentifierWithPredicates) {
                Map<QName, Object> predicates = ((NodeIdentifierWithPredicates) pathArgument).getKeyValues();

                for (QName keyValue : predicates.keySet()) {
                    String predicateValue = String.valueOf(predicates.get(keyValue));
                    textContent.append('[');
                    writeIdentifierWithNamespacePrefix(element, textContent, keyValue, prefixes);
                    textContent.append("='");
                    textContent.append(predicateValue);
                    textContent.append("']");
                }
            } else if (pathArgument instanceof NodeWithValue) {
                textContent.append("[.='");
                textContent.append(((NodeWithValue) pathArgument).getValue());
                textContent.append("']");
            }
        }
        element.setTextContent(textContent.toString());
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
            return new InstanceIdentifier.NodeIdentifier(mainQName);
        } else {
            return new InstanceIdentifier.NodeIdentifierWithPredicates(mainQName, predicates);
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
            throw new IllegalArgumentException("I wasn't possible to get namespace for prefix " + prefix);
        }

        Module youngestModule = findYoungestModuleByNamespace(schemaContext, namespace);
        return QName.create(namespace, youngestModule.getRevision(), identifier);
    }

    // FIXME: this method should be provided by SchemaContext
    private static Module findYoungestModuleByNamespace(final SchemaContext schemaContext, final URI namespace) {
        Module result = null;
        for (Module module : schemaContext.findModuleByNamespace(namespace)) {
            if (result != null) {
                if (module.getRevision().after(result.getRevision())) {
                    result = module;
                }
            } else {
                result = module;
            }
        }
        return result;
    }

    private static String toPredicateValue(final String predicatedValue) {
        String predicatedValueTrimmed = predicatedValue.trim();
        if ((predicatedValueTrimmed.startsWith(DQUOTE) || predicatedValueTrimmed.startsWith(SQUOTE))
                && (predicatedValueTrimmed.endsWith(DQUOTE) || predicatedValueTrimmed.endsWith(SQUOTE))) {
            return predicatedValueTrimmed.substring(1, predicatedValueTrimmed.length() - 1);
        }
        return null;
    }

    private static void writeIdentifierWithNamespacePrefix(final Element element, final StringBuilder textContent, final QName qName,
            final Map<String, String> prefixes) {
        String namespace = qName.getNamespace().toString();
        String prefix = prefixes.get(namespace);
        if (prefix == null) {
            prefix = qName.getPrefix();
            if (prefix == null || prefix.isEmpty() || prefixes.containsValue(prefix)) {
                prefix = generateNewPrefix(prefixes.values());
            }
        }

        element.setAttribute("xmlns:" + prefix, namespace.toString());
        textContent.append(prefix);
        prefixes.put(namespace, prefix);

        textContent.append(':');
        textContent.append(qName.getLocalName());
    }

    private static String generateNewPrefix(final Collection<String> prefixes) {
        String result;

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append('a' + random.nextInt(25));
            }

            result = sb.toString();
        } while (prefixes.contains(result));

        return result;
    }
}
