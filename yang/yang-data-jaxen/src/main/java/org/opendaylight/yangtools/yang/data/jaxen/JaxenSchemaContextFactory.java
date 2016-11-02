/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContextFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public final class JaxenSchemaContextFactory implements XPathSchemaContextFactory {
    @Nonnull
    @Override
    public XPathSchemaContext createContext(@Nonnull final SchemaContext context) {
        return new JaxenSchemaContext(context);
    }
}
