/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * SchemaNode represents a node in schema tree.
 */
public interface SchemaNode extends DocumentedNode.WithStatus {
    /**
     * Returns QName of the instance of the type <code>SchemaNode</code>.
     *
     * @return QName with the name of the schema node
     */
    @NonNull QName getQName();

    /**
     * Returns the schema path of the instance of the type
     * <code>SchemaNode</code> <code>SchemaNode</code>.
     *
     * @return schema path of the schema node
     */
    @NonNull SchemaPath getPath();
}
