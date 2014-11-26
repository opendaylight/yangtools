/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Basic entry point to Jaxen-based XPath evaluation. Each instance is bound to a particular {@link SchemaContext},
 * which it uses for namespace resolution.
 */
public final class JaxenSchemaContext {
    private final SchemaContext ctx;

    public JaxenSchemaContext(final SchemaContext ctx) {
        this.ctx = Preconditions.checkNotNull(ctx);
    }

    public JaxenNamespaceContext globalNamespaceContext() {
        return null;
    }

    public JaxenNamespaceContext moduleNamespaceContext(final Module module) {
        // FIXME: maintain a cache of these
        return ModuleNamespaceContext.create(ctx, module);
    }
}
