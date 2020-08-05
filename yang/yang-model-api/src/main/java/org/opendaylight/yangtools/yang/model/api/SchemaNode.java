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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

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

    @NonNull EffectiveStatement<QName, ?> asEffectiveStatement();

    /**
     * Returns the schema path of the instance of the type {@code SchemaNode}.
     *
     * @return schema path of the schema node
     * @throws UnsupportedOperationException when the implementation does not support per-node unique paths
     * @deprecated The idea of identifying SchemaNodes through a global path does not work. There are two problems:
     *             <ul>
     *               <li>SchemaPath does not work because it does not discern namespaces, i.e. we do not known whether
     *                   the {@code QName} refers to a {@code grouping}, a {@code typedef} or a {@code container}.
     *               </li>
     *               <li>Such a path needs to be maintained by each SchemaNode and requires us to instantiate each
     *                   effective statement as a separate object (because {@link #getPath()} is effectively an
     *                   identity within a given {@link EffectiveModelContext}.
     *               </li>
     *             </ul>
     */
    @Deprecated
    @NonNull SchemaPath getPath();
}
