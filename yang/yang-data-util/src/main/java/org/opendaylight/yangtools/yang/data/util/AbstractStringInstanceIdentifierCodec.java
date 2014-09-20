/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.codec.InstanceIdentifierCodec;

/**
 * Abstract utility class for representations which encode {@link YangInstanceIdentifier} as a
 * prefix:name tuple. Typical uses are RESTCONF/JSON (module:name) and XML (prefix:name).
 */
@Beta
public abstract class AbstractStringInstanceIdentifierCodec extends AbstractNamespaceCodec implements InstanceIdentifierCodec<String> {
    private static final Pattern PREDICATE_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    @Override
    public final String serialize(final YangInstanceIdentifier data) {
        StringBuilder sb = new StringBuilder();
        for (PathArgument arg : data.getPathArguments()) {
            if(arg instanceof AugmentationIdentifier) {
                /*
                 * XML/YANG instance identifier does not have concept
                 * of augmentation identifier, which identifies
                 * mixin (same as paretn element), so we can safely
                 * ignore it if it is part of path (since child node)
                 * is identified in same fashion.
                 *
                 */
                continue;
            }

            sb.append('/');
            appendQName(sb, arg.getNodeType());

            if (arg instanceof NodeIdentifierWithPredicates) {
                for (Map.Entry<QName, Object> entry : ((NodeIdentifierWithPredicates) arg).getKeyValues().entrySet()) {
                    sb.append('[');
                    appendQName(sb, entry.getKey());
                    sb.append("='");
                    sb.append(String.valueOf(entry.getValue()));
                    sb.append("']");
                }
            } else if (arg instanceof NodeWithValue) {
                sb.append("[.='");
                sb.append(((NodeWithValue) arg).getValue());
                sb.append("']");
            }
        }

        return sb.toString();
    }

    @Override
    public final YangInstanceIdentifier deserialize(final String data) {
        Preconditions.checkNotNull(data, "Data may not be null");

        final Iterator<String> xPathParts = SLASH_SPLITTER.split(data).iterator();

        // must be at least "/pr:node"
        if (!xPathParts.hasNext() || !xPathParts.next().isEmpty() || !xPathParts.hasNext()) {
            return null;
        }

        List<PathArgument> result = new ArrayList<>();
        while (xPathParts.hasNext()) {
            String xPathPartTrimmed = xPathParts.next().trim();

            PathArgument pathArgument = toPathArgument(xPathPartTrimmed);
            if (pathArgument != null) {
                result.add(pathArgument);
            }
        }
        return YangInstanceIdentifier.create(result);
    }

    private PathArgument toPathArgument(final String xPathArgument) {
        final QName mainQName = parseQName(xPathArgument);

        // predicates
        final Matcher matcher = PREDICATE_PATTERN.matcher(xPathArgument);
        final Map<QName, Object> predicates = new LinkedHashMap<>();
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
                    currentQName = parseQName(predicateStr.substring(0, indexOfEqualityMark));
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

    private static String trimIfEndIs(final String str, final char end) {
        final int l = str.length() - 1;
        if (str.charAt(l) != end) {
            return null;
        }

        return str.substring(1, l);
    }
}
