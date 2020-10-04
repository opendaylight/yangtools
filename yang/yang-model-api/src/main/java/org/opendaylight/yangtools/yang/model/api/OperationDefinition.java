/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Collection;

/**
 * Common interface for an operation.
 */
@Beta
public interface OperationDefinition extends SchemaNode {
    /**
     * Returns the set of type definitions declared under this operation.
     *
     * @return Set of type definitions declared under this operation.
     */
    Collection<? extends TypeDefinition<?>> getTypeDefinitions();

    /**
     * Returns the set of grouping statements declared under this operation.
     *
     * @return Set of grouping statements declared under this operation.
     */
    Collection<? extends GroupingDefinition> getGroupings();

    /**
     * Returns definition of input parameters for this operation.
     *
     * @return Definition of input parameters for this operation.
     *         The substatements of input define nodes under the operation's input node.
     */
    InputSchemaNode getInput();

    /**
     * Returns definition of output parameters for this operation.
     *
     * @return Definition of output parameters for this operation. The
     *         substatements of output define nodes under the operation's output node.
     */
    OutputSchemaNode getOutput();
}
