package org.opendaylight.yangtools.yang.parser.builder.util;

import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.builder.api.DocumentedNodeBuilder;

public abstract class AbstractDocumentedNode implements DocumentedNode {

    private final String description;
    private final String reference;
    private final Status status;

    protected AbstractDocumentedNode(final DocumentedNodeBuilder builder) {
        this.description = builder.getDescription();
        this.reference = builder.getReference();
        this.status = builder.getStatus();
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getReference() {
        return reference;
    }

    @Override
    public final Status getStatus() {
        return status;
    }

}
