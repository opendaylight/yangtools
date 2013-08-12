/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.RefineHolder;

/**
 * Interface for builders of 'uses' statement.
 */
public interface UsesNodeBuilder extends GroupingMember, Builder {

    DataNodeContainerBuilder getParent();

    String getGroupingName();

    SchemaPath getGroupingPath();

    void setGroupingPath(SchemaPath groupingPath);

    Set<AugmentationSchemaBuilder> getAugmentations();

    void addAugment(AugmentationSchemaBuilder builder);

    boolean isAugmenting();

    void setAugmenting(boolean augmenting);

    List<RefineHolder> getRefines();

    List<SchemaNodeBuilder> getRefineNodes();

    void addRefine(RefineHolder refine);

    void addRefineNode(DataSchemaNodeBuilder refineNode);

    UsesNode build();

    Set<DataSchemaNodeBuilder> getFinalChildren();

    Set<DataSchemaNodeBuilder> getTargetChildren();

    void setTargetChildren(Set<DataSchemaNodeBuilder> targetChildren);

    Set<GroupingBuilder> getFinalGroupings();

    Set<GroupingBuilder> getTargetGroupings();

    void setTargetGroupings(Set<GroupingBuilder> targetGroupings);

    Set<TypeDefinitionBuilder> getFinalTypedefs();

    Set<TypeDefinitionBuilder> getTargetTypedefs();

    void setTargetTypedefs(Set<TypeDefinitionBuilder> targetTypedefs);

    List<UnknownSchemaNodeBuilder> getFinalUnknownNodes();

    List<UnknownSchemaNodeBuilder> getTargetUnknownNodes();

    void setTargetUnknownNodes(List<UnknownSchemaNodeBuilder> targetUnknownNodes);

    List<UsesNodeBuilder> getTargetGroupingUses();

    boolean isLoadDone();

    void setLoadDone(boolean loadDone);

}
