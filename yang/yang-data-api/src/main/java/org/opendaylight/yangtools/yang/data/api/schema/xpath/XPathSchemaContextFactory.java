/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A factory for obtaining {@link XPathSchemaContext}s. This is the primary entry point to an XPath evaluation
 * implementation. Users are expected to resolve these via their service resolution framework, be it
 * {@link java.util.ServiceLoader}, OSGi or similar.
 *
 * <p>
 * Implementations are required to support {@link java.util.ServiceLoader}.
 */
@Deprecated
@NonNullByDefault
public interface XPathSchemaContextFactory {
    /**
     * Create an {@link XPathSchemaContext} based on a {@link SchemaContext}. This effectively binds the namespaces
     * the user expects to map to YANG schema. The {@link XPathExpression} compilation, relocation and optimization
     * processes can take advantage of the YANG schema provided.
     *
     * @param context SchemaContext associated with the resulting {@link XPathSchemaContext}
     * @return An {@link XPathSchemaContext} instance
     */
    XPathSchemaContext createContext(SchemaContext context);
}
