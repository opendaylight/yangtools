/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ChoiceCaseBuilder extends AbstractDataNodeContainerBuilder implements DataSchemaNodeBuilder,
        AugmentationTargetBuilder {
    private boolean isBuilt;
    private final ChoiceCaseNodeImpl instance;
    // SchemaNode args
    private SchemaPath schemaPath;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    public ChoiceCaseBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new ChoiceCaseNodeImpl(qname, path);
        constraints = new ConstraintsBuilder(moduleName, line);
    }

    public ChoiceCaseBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path, final ChoiceCaseNode base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new ChoiceCaseNodeImpl(qname, path);
        constraints = new ConstraintsBuilder(moduleName, line, base.getConstraints());

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.augmenting = base.isAugmenting();
        instance.addedByUses = base.isAddedByUses();
        instance.constraints = base.getConstraints();
        instance.augmentations.addAll(base.getAvailableAugmentations());

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        addedChildNodes.addAll(ParserUtils.wrapChildNodes(moduleName, line, base.getChildNodes(), path, ns, rev, pref));
        addedGroupings.addAll(ParserUtils.wrapGroupings(moduleName, line, base.getGroupings(), path, ns, rev, pref));

        instance.uses.addAll(base.getUses());
        instance.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public ChoiceCaseNode build() {
        if (!isBuilt) {
            instance.setConstraints(constraints.build());

            // CHILD NODES
            for (DataSchemaNodeBuilder node : addedChildNodes) {
                childNodes.add(node.build());
            }
            instance.addChildNodes(childNodes);

            // USES
            for (UsesNodeBuilder builder : addedUsesNodes) {
                usesNodes.add(builder.build());
            }
            instance.addUses(usesNodes);

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.addUnknownSchemaNodes(unknownNodes);

            // AUGMENTATIONS
            for (AugmentationSchemaBuilder builder : augmentationBuilders) {
                augmentations.add(builder.build());
            }
            instance.addAvailableAugmentations(new HashSet<>(augmentations));

            isBuilt = true;
        }

        return instance;
    }


    @Override
    public SchemaPath getPath() {
        return instance.path;
    }

    @Override
    public void setPath(SchemaPath path) {
        instance.path = path;
    }

    public String getDescription() {
        return instance.description;
    }

    @Override
    public void setDescription(final String description) {
        instance.description = description;
    }

    public String getReference() {
        return instance.reference;
    }

    @Override
    public void setReference(String reference) {
        instance.reference = reference;
    }

    public Status getStatus() {
        return instance.status;
    }

    @Override
    public void setStatus(Status status) {
        if (status != null) {
            instance.status = status;
        }
    }

    @Override
    public boolean isAugmenting() {
        return instance.augmenting;
    }

    @Override
    public void setAugmenting(boolean augmenting) {
        instance.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return instance.addedByUses;
    }

    @Override
    public void setAddedByUses(boolean addedByUses) {
        instance.addedByUses = addedByUses;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return Collections.emptySet();
    }

    @Override
    public void addTypedef(TypeDefinitionBuilder typedefBuilder) {
        throw new YangParseException(moduleName, line, "Can not add type definition to choice case.");
    }

    @Override
    public Boolean isConfiguration() {
        return false;
    }

    @Override
    public void setConfiguration(final Boolean configuration) {
        throw new YangParseException(moduleName, line, "Can not add config statement to choice case.");
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    @Override
    public void addAugmentation(AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChoiceCaseBuilder other = (ChoiceCaseBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (parentBuilder == null) {
            if (other.parentBuilder != null) {
                return false;
            }
        } else if (!parentBuilder.equals(other.parentBuilder)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "case " + getQName().getLocalName();
    }

    private static final class ChoiceCaseNodeImpl implements ChoiceCaseNode {
        private final QName qname;
        private SchemaPath path;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private boolean augmenting;
        private boolean addedByUses;
        private ConstraintDefinition constraints;
        private final Set<DataSchemaNode> childNodes = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<AugmentationSchema> augmentations = new HashSet<>();
        private final Set<UsesNode> uses = new HashSet<>();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

        private ChoiceCaseNodeImpl(QName qname, SchemaPath path) {
            this.qname = qname;
            this.path = path;
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
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraints;
        }

        private void setConstraints(ConstraintDefinition constraints) {
            this.constraints = constraints;
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
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void addUnknownSchemaNodes(List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
            }
        }

        /**
         * Always returns an empty set, because case node can not contains type
         * definitions.
         */
        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return Collections.emptySet();
        }

        @Override
        public Set<DataSchemaNode> getChildNodes() {
            return Collections.unmodifiableSet(childNodes);
        }

        private void addChildNodes(Set<DataSchemaNode> childNodes) {
            if (childNodes != null) {
                this.childNodes.addAll(childNodes);
            }
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return Collections.emptySet();
        }

        @Override
        public DataSchemaNode getDataChildByName(QName name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public DataSchemaNode getDataChildByName(String name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public Set<UsesNode> getUses() {
            return Collections.unmodifiableSet(uses);
        }

        private void addUses(Set<UsesNode> uses) {
            if (uses != null) {
                this.uses.addAll(uses);
            }
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return Collections.unmodifiableSet(augmentations);
        }

        private void addAvailableAugmentations(Set<AugmentationSchema> augmentations) {
            if (augmentations != null) {
                this.augmentations.addAll(augmentations);
            }
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
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChoiceCaseNodeImpl other = (ChoiceCaseNodeImpl) obj;
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
            StringBuilder sb = new StringBuilder(ChoiceCaseNodeImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=");
            sb.append(qname);
            sb.append("]");
            return sb.toString();
        }
    }

}
