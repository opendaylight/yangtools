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
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class UsesNodeBuilderImpl extends AbstractBuilder implements UsesNodeBuilder {
    private boolean isBuilt;
    private UsesNodeImpl instance;
    private DataNodeContainerBuilder parentBuilder;
    private final String groupingPathString;
    private SchemaPath groupingPath;
    private GroupingDefinition groupingDefinition;
    private GroupingBuilder groupingBuilder;
    private boolean addedByUses;
    private boolean augmenting;
    private boolean resolved;
    private final Set<AugmentationSchemaBuilder> addedAugments = new HashSet<>();
    private final List<SchemaNodeBuilder> refineBuilders = new ArrayList<>();
    private final List<RefineHolder> refines = new ArrayList<>();


    public UsesNodeBuilderImpl(final String moduleName, final int line, final String groupingName) {
        super(moduleName, line);
        this.groupingPathString = groupingName;
    }

    @Override
    public UsesNode build() {
        if (!isBuilt) {
            instance = new UsesNodeImpl(groupingPath);
            instance.setAddedByUses(addedByUses);

            // AUGMENTATIONS
            final Set<AugmentationSchema> augments = new HashSet<>();
            for (AugmentationSchemaBuilder builder : addedAugments) {
                augments.add(builder.build());
            }
            instance.setAugmentations(augments);

            // REFINES
            final Map<SchemaPath, SchemaNode> refineNodes = new HashMap<>();
            for (SchemaNodeBuilder refineBuilder : refineBuilders) {
                SchemaNode refineNode = refineBuilder.build();
                refineNodes.put(refineNode.getPath(), refineNode);
            }
            instance.setRefines(refineNodes);

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.addUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }

        return instance;
    }

    @Override
    public DataNodeContainerBuilder getParent() {
        return parentBuilder;
    }

    @Override
    public void setParent(final Builder parent) {
        if (!(parent instanceof DataNodeContainerBuilder)) {
            throw new YangParseException(moduleName, line,
                    "Parent of 'uses' has to be instance of DataNodeContainerBuilder, but was: '" + parent + "'.");
        }
        this.parentBuilder = (DataNodeContainerBuilder) parent;
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
    public void setGroupingDefinition(final GroupingDefinition groupingDefinition) {
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
    public void setGrouping(final GroupingBuilder grouping) {
        this.groupingBuilder = grouping;
        if (groupingBuilder != null) {
            this.groupingPath = groupingBuilder.getPath();
        }
    }

    @Override
    public String getGroupingPathAsString() {
        return groupingPathString;
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
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public void setAugmenting(final boolean augmenting) {
        this.augmenting = augmenting;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public void setResolved(final boolean resolved) {
        this.resolved = resolved;
    }

    @Override
    public List<SchemaNodeBuilder> getRefineNodes() {
        return refineBuilders;
    }

    @Override
    public void addRefineNode(final DataSchemaNodeBuilder refineNode) {
        refineBuilders.add(refineNode);
    }

    @Override
    public List<RefineHolder> getRefines() {
        return refines;
    }

    @Override
    public void addRefine(final RefineHolder refine) {
        refines.add(refine);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupingPathString == null) ? 0 : groupingPathString.hashCode());
        result = prime * result + ((parentBuilder == null) ? 0 : parentBuilder.hashCode());
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
        UsesNodeBuilderImpl other = (UsesNodeBuilderImpl) obj;
        if (groupingPathString == null) {
            if (other.groupingPathString != null) {
                return false;
            }
        } else if (!groupingPathString.equals(other.groupingPathString)) {
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
        return "uses '" + groupingPathString + "'";
    }

    private static final class UsesNodeImpl implements UsesNode {
        private final SchemaPath groupingPath;
        private Set<AugmentationSchema> augmentations = Collections.emptySet();
        private boolean addedByUses;
        private Map<SchemaPath, SchemaNode> refines = Collections.emptyMap();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

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

        private void setRefines(final Map<SchemaPath, SchemaNode> refines) {
            if (refines != null) {
                this.refines = refines;
            }
        }

        private void addUnknownSchemaNodes(final List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes.addAll(unknownSchemaNodes);
            }
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
