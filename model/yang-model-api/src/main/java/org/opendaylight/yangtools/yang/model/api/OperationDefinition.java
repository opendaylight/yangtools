/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Common interface for an operation, such as an {@link RpcDefinition} or an {@link ActionDefinition}.
 */
public sealed interface OperationDefinition extends SchemaNode permits ActionDefinition, RpcDefinition {
    /**
     * Returns the set of type definitions declared under this operation.
     *
     * @return Set of type definitions declared under this operation.
     */
    Collection<? extends @NonNull TypeDefinition<?>> getTypeDefinitions();

    /**
     * Returns the set of grouping statements declared under this operation.
     *
     * @return Set of grouping statements declared under this operation.
     */
    Collection<? extends @NonNull GroupingDefinition> getGroupings();

    /**
     * Returns definition of input parameters for this operation.
     *
     * @return Definition of input parameters for this operation.
     *         The substatements of input define nodes under the operation's input node.
     */
    @NonNull InputSchemaNode getInput();

    /**
     * Returns definition of output parameters for this operation.
     *
     * @return Definition of output parameters for this operation. The
     *         substatements of output define nodes under the operation's output node.
     */
    @NonNull OutputSchemaNode getOutput();
}
