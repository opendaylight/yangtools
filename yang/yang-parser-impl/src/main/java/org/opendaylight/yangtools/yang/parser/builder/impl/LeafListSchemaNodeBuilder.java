/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractTypeAwareBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class LeafListSchemaNodeBuilder extends AbstractTypeAwareBuilder implements DataSchemaNodeBuilder {
    private LeafListSchemaNodeImpl instance;
    private boolean userOrdered;
    // SchemaNode args
    private SchemaPath schemaPath;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    // DataSchemaNode args
    private boolean augmenting;
    private boolean addedByUses;
    private LeafListSchemaNode originalNode;
    private LeafListSchemaNodeBuilder originalBuilder;
    private boolean configuration;
    private final ConstraintsBuilder constraints;

    public LeafListSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    public LeafListSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final LeafListSchemaNode base) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        description = base.getDescription();
        reference = base.getReference();
        status = base.getStatus();
        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        originalNode = base;
        configuration = base.isConfiguration();
        this.type = base.getType();
        userOrdered = base.isUserOrdered();
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public LeafListSchemaNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new LeafListSchemaNodeImpl(qname, schemaPath);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;
        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;
        instance.configuration = configuration;
        instance.constraintsDef = constraints.toInstance();
        instance.userOrdered = userOrdered;

        if (type == null) {
            instance.type = typedef.build();
        } else {
            instance.type = type;
        }

        // ORIGINAL NODE
        if (originalNode == null && originalBuilder != null) {
            originalNode = originalBuilder.build();
        }
        instance.original = originalNode;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(final SchemaPath path) {
        this.schemaPath = path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final Status status) {
        this.status = Preconditions.checkNotNull(status, "status cannot be null");
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public void setAugmenting(final boolean augmenting) {
        this.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public LeafListSchemaNodeBuilder getOriginal() {
        return originalBuilder;
    }

    @Override
    public void setOriginal(final SchemaNodeBuilder builder) {
        Preconditions.checkArgument(builder instanceof LeafListSchemaNodeBuilder, "Original of leaf-list cannot be "
                + builder);
        this.originalBuilder = (LeafListSchemaNodeBuilder) builder;
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(final boolean configuration) {
        this.configuration = configuration;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    public boolean isUserOrdered() {
        return userOrdered;
    }

    public void setUserOrdered(final boolean userOrdered) {
        this.userOrdered = userOrdered;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LeafListSchemaNodeBuilder other = (LeafListSchemaNodeBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "leaf-list " + qname.getLocalName();
    }

}
