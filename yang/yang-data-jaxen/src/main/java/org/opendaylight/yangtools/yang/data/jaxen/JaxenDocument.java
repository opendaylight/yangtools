/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class JaxenDocument implements XPathDocument {
    private final NormalizedNode<?, ?> root;
    private final SchemaContext context;

    JaxenDocument(final JaxenSchemaContext context, final NormalizedNode<?, ?> root) {
        this.root = requireNonNull(root);
        this.context = context.getSchemaContext();
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> getRootNode() {
        return root;
    }

    @Nonnull
    SchemaContext getSchemaContext() {
        return context;
    }
}
