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
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.jaxen.JaxenException;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@NonNullByDefault
final class JaxenSchemaContext implements XPathSchemaContext {
    private final DataSchemaContextTree tree;
    private final SchemaContext context;

    JaxenSchemaContext(final SchemaContext context) {
        this.context = requireNonNull(context);
        this.tree = DataSchemaContextTree.from(context);
    }

    @Override
    public XPathExpression compileExpression(final SchemaPath schemaPath,
            final Converter<String, QNameModule> prefixes, final String xpath) throws XPathExpressionException {
        try {
            return JaxenXPath.create(prefixes, schemaPath, xpath);
        } catch (JaxenException e) {
            throw new XPathExpressionException(e);
        }
    }

    @Override
    public XPathDocument createDocument(final NormalizedNode<?, ?> documentRoot) {
        return new JaxenDocument(context, tree, documentRoot);
    }
}
