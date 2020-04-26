/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNull;

// FIXME: 6.0.0: remove this contract
public interface SchemaContextProvider {
    /**
     * Return the {@link SchemaContext} attached to this object.
     *
     * @return An SchemaContext instance.
     * @throws IllegalStateException if the context is not available.
     */
    @NonNull SchemaContext getSchemaContext();
}
