/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.YangNode;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

/**
 * Interface for builders of 'grouping' statement.
 */
public interface GroupingBuilder extends DataNodeContainerBuilder, SchemaNodeBuilder, GroupingMember {

    /**
     * Build GroupingDefinition object from this builder.
     */
    GroupingDefinition build(YangNode parent);

    void setQName(QName qname);

    Set<DataSchemaNodeBuilder> instantiateChildNodes(Builder newParent);

    Set<TypeDefinitionBuilder> instantiateTypedefs(Builder newParent);

    Set<GroupingBuilder> instantiateGroupings(Builder newParent);

    Set<UnknownSchemaNodeBuilder> instantiateUnknownNodes(Builder newParent);

}
