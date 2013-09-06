/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class UsesNodeBuilderImpl extends AbstractBuilder implements UsesNodeBuilder {
    private boolean isBuilt;
    private UsesNodeImpl instance;
    private DataNodeContainerBuilder parent;
    private final String groupingName;
    private SchemaPath groupingPath;
    private GroupingDefinition groupingDefinition;
    private GroupingBuilder groupingBuilder;
    private boolean addedByUses;
    private final Set<AugmentationSchemaBuilder> addedAugments = new HashSet<AugmentationSchemaBuilder>();
    private final List<SchemaNodeBuilder> refineBuilders = new ArrayList<SchemaNodeBuilder>();
    private final List<RefineHolder> refines = new ArrayList<RefineHolder>();

    private Set<DataSchemaNodeBuilder> targetChildren = new HashSet<>();
    private Set<GroupingBuilder> targetGroupings = new HashSet<>();
    private Set<TypeDefinitionBuilder> targetTypedefs = new HashSet<>();
    private List<UnknownSchemaNodeBuilder> targetUnknownNodes = new ArrayList<>();

    private final boolean isCopy;
    private boolean dataCollected;

    @Override
    public boolean isCopy() {
        return isCopy;
    }

    @Override
    public boolean isDataCollected() {
        return dataCollected;
    }

    @Override
    public void setDataCollected(boolean dataCollected) {
        this.dataCollected = dataCollected;
    }

    public UsesNodeBuilderImpl(final String moduleName, final int line, final String groupingName) {
        super(moduleName, line);
        this.groupingName = groupingName;
        isCopy = false;
    }

    public UsesNodeBuilderImpl(final String moduleName, final int line, final String groupingName, final boolean isCopy) {
        super(moduleName, line);
        this.groupingName = groupingName;
        this.isCopy = isCopy;
    }

    @Override
    public UsesNode build() {
        if (!isBuilt) {
            instance = new UsesNodeImpl(groupingPath);
            instance.setAddedByUses(addedByUses);

            // AUGMENTATIONS
            final Set<AugmentationSchema> augments = new HashSet<AugmentationSchema>();
            for (AugmentationSchemaBuilder builder : addedAugments) {
                augments.add(builder.build());
            }
            instance.setAugmentations(augments);

            // REFINES
            final Map<SchemaPath, SchemaNode> refineNodes = new HashMap<SchemaPath, SchemaNode>();
            for (SchemaNodeBuilder refineBuilder : refineBuilders) {
                SchemaNode refineNode = refineBuilder.build();
                refineNodes.put(refineNode.getPath(), refineNode);
            }
            instance.setRefines(refineNodes);

            // UNKNOWN NODES
            List<UnknownSchemaNode> unknownNodes = new ArrayList<UnknownSchemaNode>();
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            instance.setUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }

        return instance;
    }

    @Override
    public DataNodeContainerBuilder getParent() {
        return parent;
    }

    @Override
    public void setParent(Builder parent) {
        if (!(parent instanceof DataNodeContainerBuilder)) {
            throw new YangParseException(moduleName, line,
                    "Parent of 'uses' has to be instance of DataNodeContainerBuilder, but was: '" + parent + "'.");
        }
        this.parent = (DataNodeContainerBuilder) parent;
    }

    @Override
    public SchemaPath getGroupingPath() {
        return groupingPath;
    }

    @Override
    public GroupingDefinition getGroupingDefinition() {
        return groupingDefinition;
    }

    @Override
    public void setGroupingDefinition(GroupingDefinition groupingDefinition) {
        this.groupingDefinition = groupingDefinition;
        if (groupingDefinition != null) {
            this.groupingPath = groupingDefinition.getPath();
        }
    }

    @Override
    public GroupingBuilder getGroupingBuilder() {
        return groupingBuilder;
    }

    @Override
    public void setGrouping(GroupingBuilder grouping) {
        this.groupingBuilder = grouping;
        if (groupingBuilder != null) {
            this.groupingPath = groupingBuilder.getPath();
        }
    }

    @Override
    public String getGroupingPathAsString() {
        return groupingName;
    }

    @Override
    public Set<AugmentationSchemaBuilder> getAugmentations() {
        return addedAugments;
    }

    @Override
    public void addAugment(final AugmentationSchemaBuilder augmentBuilder) {
        addedAugments.add(augmentBuilder);
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
    public List<SchemaNodeBuilder> getRefineNodes() {
        return refineBuilders;
    }

    @Override
    public void addRefineNode(DataSchemaNodeBuilder refineNode) {
        refineBuilders.add(refineNode);
    }

    @Override
    public List<RefineHolder> getRefines() {
        return refines;
    }

    @Override
    public void addRefine(RefineHolder refine) {
        refines.add(refine);
    }

    @Override
    public Set<DataSchemaNodeBuilder> getTargetChildren() {
        return targetChildren;
    }

    @Override
    public void setTargetChildren(Set<DataSchemaNodeBuilder> targetChildren) {
        this.targetChildren = targetChildren;
    }

    @Override
    public Set<GroupingBuilder> getTargetGroupings() {
        return targetGroupings;
    }

    @Override
    public void setTargetGroupings(Set<GroupingBuilder> targetGroupings) {
        this.targetGroupings = targetGroupings;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTargetTypedefs() {
        return targetTypedefs;
    }

    @Override
    public void setTargetTypedefs(Set<TypeDefinitionBuilder> targetTypedefs) {
        this.targetTypedefs = targetTypedefs;
    }

    @Override
    public List<UnknownSchemaNodeBuilder> getTargetUnknownNodes() {
        return targetUnknownNodes;
    }

    @Override
    public void setTargetUnknownNodes(List<UnknownSchemaNodeBuilder> targetUnknownNodes) {
        this.targetUnknownNodes = targetUnknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupingName == null) ? 0 : groupingName.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((refines == null) ? 0 : refines.hashCode());
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
        UsesNodeBuilderImpl other = (UsesNodeBuilderImpl) obj;
        if (groupingName == null) {
            if (other.groupingName != null) {
                return false;
            }
        } else if (!groupingName.equals(other.groupingName)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        if (refines == null) {
            if (other.refines != null) {
                return false;
            }
        } else if (!refines.equals(other.refines)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uses '" + groupingName + "'";
    }

    public final class UsesNodeImpl implements UsesNode {
        private final SchemaPath groupingPath;
        private Set<AugmentationSchema> augmentations = Collections.emptySet();
        private boolean addedByUses;
        private Map<SchemaPath, SchemaNode> refines = Collections.emptyMap();
        private List<UnknownSchemaNode> unknownNodes = Collections.emptyList();

        private UsesNodeImpl(final SchemaPath groupingPath) {
            this.groupingPath = groupingPath;
        }

        @Override
        public SchemaPath getGroupingPath() {
            return groupingPath;
        }

        @Override
        public Set<AugmentationSchema> getAugmentations() {
            return augmentations;
        }

        private void setAugmentations(final Set<AugmentationSchema> augmentations) {
            if (augmentations != null) {
                this.augmentations = augmentations;
            }
        }

        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        private void setAddedByUses(final boolean addedByUses) {
            this.addedByUses = addedByUses;
        }

        @Override
        public Map<SchemaPath, SchemaNode> getRefines() {
            return refines;
        }

        private void setRefines(Map<SchemaPath, SchemaNode> refines) {
            if (refines != null) {
                this.refines = refines;
            }
        }

        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        private void setUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes = unknownSchemaNodes;
            }
        }

        public UsesNodeBuilder toBuilder() {
            return UsesNodeBuilderImpl.this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((groupingPath == null) ? 0 : groupingPath.hashCode());
            result = prime * result + ((augmentations == null) ? 0 : augmentations.hashCode());
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
            final UsesNodeImpl other = (UsesNodeImpl) obj;
            if (groupingPath == null) {
                if (other.groupingPath != null) {
                    return false;
                }
            } else if (!groupingPath.equals(other.groupingPath)) {
                return false;
            }
            if (augmentations == null) {
                if (other.augmentations != null) {
                    return false;
                }
            } else if (!augmentations.equals(other.augmentations)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(UsesNodeImpl.class.getSimpleName());
            sb.append("[groupingPath=");
            sb.append(groupingPath);
            sb.append("]");
            return sb.toString();
        }
    }

}
