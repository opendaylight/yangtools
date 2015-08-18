/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Builder for {@link SchemaNode}.
 */
public interface SchemaNodeBuilder extends DocumentedNodeBuilder, Builder {

    /**
     * Returns qname of resulting {@link SchemaNode}.
     *
     * @return QName of this node
     */
    QName getQName();

    /**
     * Returns schema path of resulting  {@link SchemaNode}.
     *
     * @return SchemaPath of this node
     */
    SchemaPath getPath();

    /**
     * Updates schema path to resulting {@link SchemaNode}.
     *
     * @param path schema path
     */
    void setPath(SchemaPath path);

    /**
     * Build SchemaNode object from this builder.
     */
    @Override
    SchemaNode build();

}
