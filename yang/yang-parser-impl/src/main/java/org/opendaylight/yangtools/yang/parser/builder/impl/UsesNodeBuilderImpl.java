/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class UsesNodeBuilderImpl extends AbstractBuilder implements UsesNodeBuilder {
    private UsesNodeImpl instance;
    private DataNodeContainerBuilder parentBuilder;
    private final SchemaPath targetGroupingPath;
    private SchemaPath groupingPath;
    private GroupingDefinition groupingDefinition;
    private GroupingBuilder groupingBuilder;
    private boolean addedByUses;
    private boolean augmenting;
    private boolean resolved;
    private final Set<AugmentationSchemaBuilder> augmentationBuilders = new HashSet<>();
    private final List<SchemaNodeBuilder> refineBuilders = new ArrayList<>();
    private final List<RefineBuilder> refines = new ArrayList<>();

    public UsesNodeBuilderImpl(final String moduleName, final int line, final SchemaPath targetGroupingPath) {
        super(moduleName, line);
        this.targetGroupingPath = targetGroupingPath;
    }

    @Override
    public UsesNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new UsesNodeImpl(groupingPath);
        instance.setAddedByUses(addedByUses);

        // AUGMENTATIONS
        final Set<AugmentationSchema> augments = new HashSet<>();
        for (AugmentationSchemaBuilder builder : augmentationBuilders) {
            if (!builder.isUnsupportedTarget()) {
                augments.add(builder.build());
            }
        }
        instance.augmentations = ImmutableSet.copyOf(augments);

        // REFINES
        final Map<SchemaPath, SchemaNode> refineNodes = new HashMap<>();
        for (SchemaNodeBuilder refineBuilder : refineBuilders) {
            SchemaNode refineNode = refineBuilder.build();
            refineNodes.put(refineNode.getPath(), refineNode);
        }
        instance.refines = ImmutableMap.copyOf(refineNodes);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public DataNodeContainerBuilder getParent() {
        return parentBuilder;
    }

    @Override
    public void setParent(final Builder parent) {
        if (!(parent instanceof DataNodeContainerBuilder)) {
            throw new YangParseException(getModuleName(), getLine(),
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
    public SchemaPath getTargetGroupingPath() {
        return targetGroupingPath;
    }

    @Override
    public Set<AugmentationSchemaBuilder> getAugmentations() {
        return augmentationBuilders;
    }

    @Override
    public void addAugment(final AugmentationSchemaBuilder augmentBuilder) {
        augmentationBuilders.add(augmentBuilder);
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
    public List<RefineBuilder> getRefines() {
        return refines;
    }

    @Override
    public void addRefine(final RefineBuilder refine) {
        refines.add(refine);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(groupingPath);
        result = prime * result + Objects.hashCode(parentBuilder);
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
        if (groupingPath == null) {
            if (other.groupingPath != null) {
                return false;
            }
        } else if (!groupingPath.equals(other.groupingPath)) {
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
        return "uses '" + groupingPath + "'";
    }

}
