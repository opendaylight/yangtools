/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Node which can have documentation assigned.
 */
public interface DocumentedNode {
    /**
     * Returns the value of the argument of YANG <code>description</code> keyword.
     *
     * @return string with the description, or null if description was not provided.
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default String getDescription() {
        return null;
    }

    /**
     * Returns the value of the argument of YANG <code>reference</code> keyword.
     *
     * @return string with reference to some other document, or null if reference was not provided.
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default String getReference() {
        return null;
    }

    /**
     * Returns unknown schema nodes which belongs to this instance.
     *
     * @return list of unknown schema nodes defined under this node.
     */
    default @Nonnull List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return ImmutableList.of();
    }

    interface WithStatus extends DocumentedNode {
        /**
         * Returns status of the instance of the type <code>SchemaNode</code>.
         *
         * @return status of this node which represents the argument of the YANG
         *         <code>status</code> substatement
         */
        @Nonnull Status getStatus();
    }
}
