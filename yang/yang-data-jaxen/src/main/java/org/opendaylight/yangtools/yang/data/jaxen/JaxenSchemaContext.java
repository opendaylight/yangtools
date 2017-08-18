/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Converter;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathExpressionException;
import org.jaxen.JaxenException;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class JaxenSchemaContext implements XPathSchemaContext {
    // Will be needed for compileExpression()
    private final SchemaContext context;

    JaxenSchemaContext(final SchemaContext context) {
        this.context = requireNonNull(context);
    }

    @Nonnull
    @Override
    public XPathExpression compileExpression(@Nonnull final SchemaPath schemaPath,
            final Converter<String, QNameModule> prefixes, @Nonnull final String xpath)
            throws XPathExpressionException {
        try {
            return JaxenXPath.create(prefixes, schemaPath, xpath);
        } catch (JaxenException e) {
            throw new XPathExpressionException(e);
        }
    }

    @Nonnull
    @Override
    public XPathDocument createDocument(@Nonnull final NormalizedNode<?, ?> documentRoot) {
        return new JaxenDocument(this, documentRoot);
    }

    @Nonnull
    SchemaContext getSchemaContext() {
        return context;
    }
}
