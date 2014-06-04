package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.Status;

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
     * @param description
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
     * @param reference
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
     * @param status
     */
    void setStatus(Status status);
}
