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
 * Common interface for an operation
 */
@Beta
public interface OperationDefinition extends SchemaNode {

    /**
     * @return Set of type definitions declared under this operation.
     */
    Set<TypeDefinition<?>> getTypeDefinitions();

    /**
     * @return Set of grouping statements declared under this operation.
     */
    Set<GroupingDefinition> getGroupings();

    /**
     * @return Definition of input parameters for this operation.
     *         The substatements of input define nodes under the operation's input node.
     */
    ContainerSchemaNode getInput();

    /**
     * @return Definition of output parameters for this operation. The
     *         substatements of output define nodes under the operation's output node.
     */
    ContainerSchemaNode getOutput();
}
