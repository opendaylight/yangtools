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

    void addRefineNode(SchemaNodeBuilder refineNode);

    UsesNode build();

}
