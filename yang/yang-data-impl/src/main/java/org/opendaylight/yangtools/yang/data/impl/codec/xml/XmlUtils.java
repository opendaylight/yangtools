/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Common XML-related utility methods, which are not specific to a particular
 * JAXP API.
 *
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public final class XmlUtils {
    public static final XmlCodecProvider DEFAULT_XML_CODEC_PROVIDER =
            TypeDefinitionAwareCodec::from;

    private XmlUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated This utility method is no longer needed: check type identity directly
     */
    @Deprecated
    public static TypeDefinition<?> resolveBaseTypeFrom(final @Nonnull TypeDefinition<?> type) {
        TypeDefinition<?> superType = type;
        while (superType.getBaseType() != null) {
            superType = superType.getBaseType();
        }
        return superType;
    }
}
