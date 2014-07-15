/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import java.util.Map;

import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Common XML-related utility methods, which are not specific to a particular
 * JAXP API.
 */
public final class XmlUtils {
    public static final XmlCodecProvider DEFAULT_XML_CODEC_PROVIDER = new XmlCodecProvider() {
        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(final TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    private XmlUtils() {
    }

    public static TypeDefinition<?> resolveBaseTypeFrom(final @Nonnull TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }

    static String encodeIdentifier(final RandomPrefix prefixes, final InstanceIdentifier id) {
        StringBuilder textContent = new StringBuilder();
        for (PathArgument pathArgument : id.getPathArguments()) {
            textContent.append('/');
            textContent.append(prefixes.encodeQName(pathArgument.getNodeType()));
            if (pathArgument instanceof NodeIdentifierWithPredicates) {
                Map<QName, Object> predicates = ((NodeIdentifierWithPredicates) pathArgument).getKeyValues();

                for (QName keyValue : predicates.keySet()) {
                    String predicateValue = String.valueOf(predicates.get(keyValue));
                    textContent.append('[');
                    textContent.append(prefixes.encodeQName(keyValue));
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

        return textContent.toString();
    }
}
