/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathDocument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.AbstractEffectiveModelContextProvider;

@NonNullByDefault
final class JaxenDocument extends AbstractEffectiveModelContextProvider implements XPathDocument {
    private final DataSchemaContextNode<?> schema;
    private final NormalizedNode<?, ?> root;

    JaxenDocument(final EffectiveModelContext context, final DataSchemaContextTree tree,
            final NormalizedNode<?, ?> root) {
        super(context);
        this.root = requireNonNull(root);
        this.schema = requireNonNull(tree.getRoot().getChild(root.getIdentifier()));
    }

    @Override
    public NormalizedNode<?, ?> getRootNode() {
        return root;
    }

    DataSchemaContextNode<?> getSchema() {
        return schema;
    }
}
