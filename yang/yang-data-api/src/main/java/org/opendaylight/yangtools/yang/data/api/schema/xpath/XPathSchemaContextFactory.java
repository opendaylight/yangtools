/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.base.Converter;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathException;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A factory for obtaining {@link XPathSchemaContext}s. This is the primary entry point to an XPath evaluation
 * implementation. Users are expected to resolve these via their service resolution framework, be it
 * {@link java.util.ServiceLoader}, OSGi or similar.
 *
 * Implementations are required to support {@link java.util.ServiceLoader}.
 */
public interface XPathSchemaContextFactory {
    /**
     * Create an {@link XPathSchemaContext} based on a {@link SchemaContext}. This effectively binds the namespaces
     * the user expects to map to YANG schema. The {@link XPathExpression} compilation, relocation and optimization
     * processes can take advantage of the YANG schema provided and the prefix mappings requested by the provided
     * mapper.
     *
     * The user must provide a prefix-to-mapping {@link Converter}, which will be used to convert any prefixes found
     * in the XPath expression being compiled in the resulting context.
     *
     * @param context SchemaContext associated with the resulting {@link XPathSchemaContext}
     * @param prefixToNamespace Prefix-to-namespace converter
     * @return An {@link XPathSchemaContext} instance
     * @throws IllegalArgumentException if the converter contains a namespace which does not exist in the supplied
     *                                  SchemaContext.
     */
    @Nonnull XPathSchemaContext createContext(@Nonnull SchemaContext context,
            Converter<String, QNameModule> prefixToNamespace) throws XPathException;
}
