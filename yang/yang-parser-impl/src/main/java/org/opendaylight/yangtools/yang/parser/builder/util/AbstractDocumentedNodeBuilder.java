package org.opendaylight.yangtools.yang.parser.builder.util;

import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.builder.api.DocumentedNodeBuilder;

public abstract class AbstractDocumentedNodeBuilder extends AbstractBuilder implements DocumentedNodeBuilder{
    private String description = "";
    private String reference = "";
    private Status status = Status.CURRENT;



    public AbstractDocumentedNodeBuilder(final String moduleName, final int line) {
        super(moduleName, line);
    }

    public AbstractDocumentedNodeBuilder(final String moduleName, final int line, final DocumentedNode node) {
        super(moduleName, line);
        description = node.getDescription();
        reference = node.getReference();
        status = node.getStatus();
    }

    @Override
    public final void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public final void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public final void setStatus(final Status status) {
        this.status  = status;
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
