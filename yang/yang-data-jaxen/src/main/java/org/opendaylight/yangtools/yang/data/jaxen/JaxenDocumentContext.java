/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;

final class JaxenDocument implements XPathDocument {
    private final NormalizedNode<?, ?> root;

    JaxenDocument(final JaxenSchemaContext context, final NormalizedNode<?, ?> root) {
        this.root = Preconditions.checkNotNull(root);
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> getRootNode() {
        return root;
    }
}
