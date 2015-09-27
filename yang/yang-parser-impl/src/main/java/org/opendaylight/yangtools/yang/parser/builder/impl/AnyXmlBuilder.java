/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;

public final class AnyXmlBuilder extends AbstractSchemaNodeBuilder implements DataSchemaNodeBuilder {
    private AnyXmlSchemaNodeImpl instance;

    private boolean augmenting;
    private boolean addedByUses;
    private boolean configuration;
    private AnyXmlSchemaNode originalNode;
    private AnyXmlBuilder originalBuilder;
    private final ConstraintsBuilder constraints;

    public AnyXmlBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    public AnyXmlBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final AnyXmlSchemaNode base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        description = base.getDescription();
        reference = base.getReference();
        status = base.getStatus();
        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        originalNode = base;
        configuration = base.isConfiguration();
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public AnyXmlSchemaNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new AnyXmlSchemaNodeImpl(qname, schemaPath);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;
        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;
        instance.configuration = configuration;
        instance.constraintsDef = constraints.build();

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
    public ConstraintsBuilder getConstraints() {
        return constraints;
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
    public AnyXmlBuilder getOriginal() {
        return originalBuilder;
    }

    @Override
    public void setOriginal(final SchemaNodeBuilder builder) {
        Preconditions.checkArgument(builder instanceof AnyXmlBuilder, "Original of anyxml cannot be " + builder);
        this.originalBuilder = (AnyXmlBuilder) builder;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(schemaPath);
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
        AnyXmlBuilder other = (AnyXmlBuilder) obj;
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
        return "anyxml " + qname.getLocalName();
    }

}
