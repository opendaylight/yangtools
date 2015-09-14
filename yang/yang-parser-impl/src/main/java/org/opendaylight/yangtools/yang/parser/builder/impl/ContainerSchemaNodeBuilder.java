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
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;

public final class ContainerSchemaNodeBuilder extends AbstractDocumentedDataNodeContainerBuilder implements
        AugmentationTargetBuilder, DataSchemaNodeBuilder {
    private ContainerSchemaNodeImpl instance;
    private boolean presence;
    // SchemaNode args
    private SchemaPath path;
    // DataSchemaNode args
    private boolean augmenting;
    private boolean addedByUses;
    private boolean configuration;
    private ContainerSchemaNode originalNode;
    private ContainerSchemaNodeBuilder originalBuilder;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    public ContainerSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.path = Preconditions.checkNotNull(path, "Schema Path must not be null");
        this.constraints = new ConstraintsBuilderImpl(moduleName, line);
    }

    // constructor for uses
    public ContainerSchemaNodeBuilder(final String moduleName, final int line, final QName qname,
            final SchemaPath path, final ContainerSchemaNode base) {
        super(moduleName, line, qname, path, base);
        this.path = Preconditions.checkNotNull(path, "Schema Path must not be null");

        constraints = new ConstraintsBuilderImpl(moduleName, line, base.getConstraints());

        augmenting = base.isAugmenting();
        addedByUses = base.isAddedByUses();
        originalNode = base;
        configuration = base.isConfiguration();
        presence = base.isPresenceContainer();

        augmentations.addAll(base.getAvailableAugmentations());

    }

    @Override
    protected String getStatementName() {
        return "container";
    }

    @Override
    public ContainerSchemaNode build() {
        if (instance != null) {
            return instance;
        }

        buildChildren();
        instance = new ContainerSchemaNodeImpl(this);

        instance.augmenting = augmenting;
        instance.addedByUses = addedByUses;
        instance.configuration = configuration;
        instance.constraints = constraints.build();
        instance.presence = presence;

        // ORIGINAL NODE
        if (originalNode == null && originalBuilder != null) {
            originalNode = originalBuilder.build();
        }
        instance.original = originalNode;

        // AUGMENTATIONS
        for (AugmentationSchemaBuilder builder : augmentationBuilders) {
            augmentations.add(builder.build());
        }
        instance.augmentations = ImmutableSet.copyOf(augmentations);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    public List<AugmentationSchemaBuilder> getAugmentationBuilders() {
        return augmentationBuilders;
    }

    @Override
    public void addAugmentation(final AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public void setPath(final SchemaPath path) {
        this.path = path;
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
    public ContainerSchemaNodeBuilder getOriginal() {
        return originalBuilder;
    }

    @Override
    public void setOriginal(final SchemaNodeBuilder builder) {
        Preconditions.checkArgument(builder instanceof ContainerSchemaNodeBuilder, "Original of container cannot be "
                + builder);
        this.originalBuilder = (ContainerSchemaNodeBuilder) builder;
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

    public boolean isPresence() {
        return presence;
    }

    public void setPresence(final boolean presence) {
        this.presence = presence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        ContainerSchemaNodeBuilder other = (ContainerSchemaNodeBuilder) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
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
        return "container " + qname.getLocalName();
    }

}
