/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.Status;

/**
 * Mixin-style builder interfac for nodes, which may have documentation attached.
 *
 **/
public interface DocumentedNodeBuilder {

    /**
     * Returns description of resulting schema node
     * as was defined by description statement.
     *
     * @return description statement
     */
    String getDescription();

    /**
     * Set description to this node.
     *
     * @param description description of this node
     */
    void setDescription(String description);

    /**
     * Get reference of this node.
     *
     * @return reference statement
     */
    String getReference();

    /**
     * Set reference to this node.
     *
     * @param reference reference to this node
     */
    void setReference(String reference);

    /**
     * Get status of this node.
     *
     * @return status statement
     */
    Status getStatus();

    /**
     * Set status to this node.
     *
     * @param status status of this node
     */
    void setStatus(Status status);
}
