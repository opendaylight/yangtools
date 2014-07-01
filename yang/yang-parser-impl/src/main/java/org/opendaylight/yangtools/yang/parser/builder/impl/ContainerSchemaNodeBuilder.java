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
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainer;
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
        instance.constraints = constraints.toInstance();
        instance.presence = presence;

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
        // FIXME: Do we really need this? This actually triggers equals
        // up to the root builder.
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

    private static final class ContainerSchemaNodeImpl extends AbstractDocumentedDataNodeContainer implements
            ContainerSchemaNode {
        private final QName qname;
        private final SchemaPath path;

        private boolean augmenting;
        private boolean addedByUses;
        private boolean configuration;
        private ConstraintDefinition constraints;

        private ImmutableSet<AugmentationSchema> augmentations;
        private ImmutableList<UnknownSchemaNode> unknownNodes;

        private boolean presence;

        public ContainerSchemaNodeImpl(final ContainerSchemaNodeBuilder builder) {
            super(builder);
            this.qname = builder.getQName();
            this.path = builder.getPath();
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public boolean isAugmenting() {
            return augmenting;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        @Override
        public boolean isConfiguration() {
            return configuration;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraints;
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return augmentations;
        }

        @Override
        public boolean isPresenceContainer() {
            return presence;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
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
            ContainerSchemaNodeImpl other = (ContainerSchemaNodeImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "container " + qname.getLocalName();
        }
    }

}
