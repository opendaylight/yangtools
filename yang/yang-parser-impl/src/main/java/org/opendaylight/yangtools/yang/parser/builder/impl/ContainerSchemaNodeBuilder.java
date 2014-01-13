/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ContainerSchemaNodeBuilder extends AbstractDataNodeContainerBuilder implements
        AugmentationTargetBuilder, DataSchemaNodeBuilder {
    private boolean isBuilt;
    private final ContainerSchemaNodeImpl instance;
    private YangNode parent;

    private final SchemaPath path;
    // DataSchemaNode args
    private Boolean configuration;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    public ContainerSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.path = path;
        this.instance = new ContainerSchemaNodeImpl(qname, path);
        this.constraints = new ConstraintsBuilder(moduleName, line);
    }

    // constructor for uses
    public ContainerSchemaNodeBuilder(final String moduleName, final int line, final QName qname,
            final SchemaPath path, final ContainerSchemaNode base) {
        super(moduleName, line, qname);
        this.path = path;
        instance = new ContainerSchemaNodeImpl(qname, path);
        constraints = new ConstraintsBuilder(moduleName, line, base.getConstraints());

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.augmenting = base.isAugmenting();
        instance.addedByUses = base.isAddedByUses();
        instance.configuration = base.isConfiguration();
        instance.constraints = base.getConstraints();
        instance.augmentations.addAll(base.getAvailableAugmentations());

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        addedChildNodes.addAll(ParserUtils.wrapChildNodes(moduleName, line, base.getChildNodes(), path, ns, rev, pref));
        addedGroupings.addAll(ParserUtils.wrapGroupings(moduleName, line, base.getGroupings(), path, ns, rev, pref));
        addedTypedefs.addAll(ParserUtils.wrapTypedefs(moduleName, line, base, path, ns, rev, pref));
        addedUnknownNodes.addAll(ParserUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path, ns,
                rev, pref));

        instance.uses.addAll(base.getUses());
        instance.presence = base.isPresenceContainer();
        instance.configuration = base.isConfiguration();
        this.configuration = base.isConfiguration();
    }

    @Override
    public ContainerSchemaNode build() {
        if (!isBuilt) {

            // if this builder represents rpc input or output, it can has
            // configuration value set to null
            if (configuration == null) {
                configuration = false;
            }
            instance.setConfiguration(configuration);

            // USES
            for (UsesNodeBuilder builder : addedUsesNodes) {
                usesNodes.add(builder.build());
            }
            instance.addUses(usesNodes);

            // CHILD NODES
            for (DataSchemaNodeBuilder node : addedChildNodes) {
                DataSchemaNode child = node.build();
                childNodes.put(child.getQName(), child);
            }
            instance.addChildNodes(childNodes);

            // GROUPINGS
            for (GroupingBuilder builder : addedGroupings) {
                groupings.add(builder.build());
            }
            instance.addGroupings(groupings);

            // TYPEDEFS
            for (TypeDefinitionBuilder entry : addedTypedefs) {
                typedefs.add(entry.build());
            }
            instance.addTypeDefinitions(typedefs);

            // AUGMENTATIONS
            final List<AugmentationSchema> augmentations = new ArrayList<>();
            for (AugmentationSchemaBuilder builder : augmentationBuilders) {
                augmentations.add(builder.build());
            }
            instance.addAvailableAugmentations(new HashSet<>(augmentations));

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.addUnknownSchemaNodes(unknownNodes);

            if (constraints != null) {
                instance.setConstraints(constraints.build());
            }

            isBuilt = true;
        }
        return instance;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return addedTypedefs;
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder type) {
        String typeName = type.getQName().getLocalName();
        for (TypeDefinitionBuilder addedTypedef : addedTypedefs) {
            if (addedTypedef.getQName().getLocalName().equals(typeName)) {
                throw new YangParseException(moduleName, type.getLine(), "Can not add typedef '" + typeName
                        + "': typedef with same name already declared at line " + addedTypedef.getLine());
            }
        }
        addedTypedefs.add(type);
    }

    public List<AugmentationSchemaBuilder> getAugmentationBuilders() {
        return augmentationBuilders;
    }

    @Override
    public void addAugmentation(AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    @Override
    public SchemaPath getPath() {
        return instance.path;
    }

    @Override
    public void setPath(SchemaPath path) {
        instance.path = path;
    }

    @Override
    public String getDescription() {
        return instance.description;
    }

    @Override
    public void setDescription(final String description) {
        instance.description = description;
    }

    @Override
    public String getReference() {
        return instance.reference;
    }

    @Override
    public void setReference(final String reference) {
        instance.reference = reference;
    }

    @Override
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
    public void setAddedByUses(final boolean addedByUses) {
        instance.addedByUses = addedByUses;
    }

    @Override
    public Boolean isConfiguration() {
        return instance.configuration;
    }

    @Override
    public void setConfiguration(Boolean configuration) {
        instance.configuration = configuration;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    public boolean isPresence() {
        return instance.presence;
    }

    public void setPresence(boolean presence) {
        instance.presence = presence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ContainerSchemaNodeBuilder other = (ContainerSchemaNodeBuilder) obj;
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "container " + qname.getLocalName();
    }

    public final class ContainerSchemaNodeImpl implements ContainerSchemaNode {
        private final QName qname;
        private SchemaPath path;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private boolean augmenting;
        private boolean addedByUses;
        private boolean configuration;
        private ConstraintDefinition constraints;
        private final Set<AugmentationSchema> augmentations = new HashSet<>();
        private final Map<QName, DataSchemaNode> childNodes = new HashMap<>();
        private final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<TypeDefinition<?>> typeDefinitions = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<UsesNode> uses = new HashSet<>();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
        private boolean presence;

        private ContainerSchemaNodeImpl(QName qname, SchemaPath path) {
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

        private void setConfiguration(boolean configuration) {
            this.configuration = configuration;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraints;
        }

        private void setConstraints(ConstraintDefinition constraints) {
            this.constraints = constraints;
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
        public Set<DataSchemaNode> getChildNodes() {
            final Set<DataSchemaNode> result = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
            result.addAll(childNodes.values());
            return Collections.unmodifiableSet(result);
        }

        private void addChildNodes(Map<QName, DataSchemaNode> childNodes) {
            if (childNodes != null) {
                this.childNodes.putAll(childNodes);
            }
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return Collections.unmodifiableSet(groupings);
        }

        private void addGroupings(Set<GroupingDefinition> groupings) {
            if (groupings != null) {
                this.groupings.addAll(groupings);
            }
        }

        @Override
        public DataSchemaNode getDataChildByName(QName name) {
            return childNodes.get(name);
        }

        @Override
        public DataSchemaNode getDataChildByName(String name) {
            DataSchemaNode result = null;
            for (Map.Entry<QName, DataSchemaNode> entry : childNodes.entrySet()) {
                if (entry.getKey().getLocalName().equals(name)) {
                    result = entry.getValue();
                    break;
                }
            }
            return result;
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
        public boolean isPresenceContainer() {
            return presence;
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return Collections.unmodifiableSet(typeDefinitions);
        }

        private void addTypeDefinitions(Set<TypeDefinition<?>> typeDefinitions) {
            if (typeDefinitions != null) {
                this.typeDefinitions.addAll(typeDefinitions);
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void addUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes.addAll(unknownSchemaNodes);
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
