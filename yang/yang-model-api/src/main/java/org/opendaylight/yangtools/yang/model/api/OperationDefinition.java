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
 * Common interface for action and rpc
 */
@Beta
public interface OperationDefinition {

    /**
     * @return Set of type definitions declared under this action/rpc statement.
     */
    Set<TypeDefinition<?>> getTypeDefinitions();

    /**
     * @return Set of grouping statements declared under this action/rpc statement.
     */
    Set<GroupingDefinition> getGroupings();

    /**
     * @return Definition of input parameters for the action/rpc operation. The
     *         substatements of input define nodes under the action's/rpc's input node.
     */
    ContainerSchemaNode getInput();

    /**
     * @return Definition of output parameters to the action/rpc operation. The
     *         substatements of output define nodes under the action's/rpc's output node.
     */
    ContainerSchemaNode getOutput();
}
