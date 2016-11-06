/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Set;

/**
 * Represents YANG action statement
 *
 * The "action" statement is used to define an operation connected to a
 * specific container or list data node.  It takes one argument, which
 * is an identifier, followed by a block of substatements that holds
 * detailed action information.  The argument is the name of the action.
 */
@Beta
public interface ActionDefinition extends SchemaNode {

    /**
     * @return Set of type definitions declared under this action statement.
     */
    Set<TypeDefinition<?>> getTypeDefinitions();

    /**
     * @return Set of grouping statements declared under this action statement.
     */
    Set<GroupingDefinition> getGroupings();

    /**
     * @return Definition of input parameters for the action operation. The
     *         substatements of input define nodes under the action's input node.
     */
    ContainerSchemaNode getInput();

    /**
     * @return Definition of output parameters to the action operation. The
     *         substatements of output define nodes under the action's output node.
     */
    ContainerSchemaNode getOutput();
}
