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

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class NotificationBuilder extends AbstractDataNodeContainerBuilder implements SchemaNodeBuilder,
        AugmentationTargetBuilder {
    private boolean isBuilt;
    private final NotificationDefinitionImpl instance;
    private SchemaPath schemaPath;
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new NotificationDefinitionImpl(qname, path);
    }

    NotificationBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path, NotificationDefinition base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new NotificationDefinitionImpl(qname, path);

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.augmentations.addAll(base.getAvailableAugmentations());

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        addedChildNodes.addAll(ParserUtils.wrapChildNodes(moduleName, line, base.getChildNodes(), path, ns, rev, pref));
        addedGroupings.addAll(ParserUtils.wrapGroupings(moduleName, line, base.getGroupings(), path, ns, rev, pref));

        instance.groupings.addAll(base.getGroupings());
        instance.typeDefinitions.addAll(base.getTypeDefinitions());
        instance.uses.addAll(base.getUses());
        instance.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public NotificationDefinition build() {
        if (!(parentBuilder instanceof ModuleBuilder)) {
            throw new YangParseException(moduleName, line, "Notification can be defined only under module (was " + parentBuilder + ")");
        }
        if (!isBuilt) {
            // CHILD NODES
            for (DataSchemaNodeBuilder node : addedChildNodes) {
                DataSchemaNode child = node.build();
                childNodes.put(child.getQName(), child);
            }
            instance.setChildNodes(childNodes);

            // GROUPINGS
            for (GroupingBuilder builder : addedGroupings) {
                groupings.add(builder.build());
            }
            instance.setGroupings(groupings);

            // TYPEDEFS
            for (TypeDefinitionBuilder entry : addedTypedefs) {
                typedefs.add(entry.build());
            }
            instance.setTypeDefinitions(typedefs);

            // USES
            for (UsesNodeBuilder builder : addedUsesNodes) {
                usesNodes.add(builder.build());
            }
            instance.setUses(usesNodes);

            // AUGMENTATIONS
            for (AugmentationSchemaBuilder builder : augmentationBuilders) {
                augmentations.add(builder.build());
            }
            instance.setAvailableAugmentations(new HashSet<>(augmentations));

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.setUnknownSchemaNodes(unknownNodes);

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
        addedTypedefs.add(type);
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
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
    public void addAugmentation(AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    @Override
    public String toString() {
        return "notification " + getQName().getLocalName();
    }

    public final class NotificationDefinitionImpl implements NotificationDefinition {
        private final QName qname;
        private final SchemaPath path;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private final Map<QName, DataSchemaNode> childNodes = new HashMap<>();
        private final Set<GroupingDefinition> groupings = new HashSet<>();
        private final Set<TypeDefinition<?>> typeDefinitions = new HashSet<>();
        private final Set<UsesNode> uses = new HashSet<>();
        private final Set<AugmentationSchema> augmentations = new HashSet<>();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

        private NotificationDefinitionImpl(final QName qname, final SchemaPath path) {
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
        public Set<DataSchemaNode> getChildNodes() {
            return Collections.unmodifiableSet(new HashSet<DataSchemaNode>(childNodes.values()));
        }

        private void setChildNodes(Map<QName, DataSchemaNode> childNodes) {
            if (childNodes != null) {
                this.childNodes.putAll(childNodes);
            }
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return Collections.unmodifiableSet(groupings);
        }

        private void setGroupings(Set<GroupingDefinition> groupings) {
            if (groupings != null) {
                this.groupings.addAll(groupings);
            }
        }

        @Override
        public Set<UsesNode> getUses() {
            return Collections.unmodifiableSet(uses);
        }

        private void setUses(Set<UsesNode> uses) {
            if (uses != null) {
                this.uses.addAll(uses);
            }
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return Collections.unmodifiableSet(typeDefinitions);
        }

        private void setTypeDefinitions(final Set<TypeDefinition<?>> typeDefinitions) {
            if (typeDefinitions != null) {
                this.typeDefinitions.addAll(typeDefinitions);
            }
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return Collections.unmodifiableSet(augmentations);
        }

        private void setAvailableAugmentations(Set<AugmentationSchema> augmentations) {
            if (augmentations != null) {
                this.augmentations.addAll(augmentations);
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
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
            final NotificationDefinitionImpl other = (NotificationDefinitionImpl) obj;
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
            StringBuilder sb = new StringBuilder(NotificationDefinitionImpl.class.getSimpleName());
            sb.append("[qname=" + qname + ", path=" + path + "]");
            return sb.toString();
        }
    }

}
