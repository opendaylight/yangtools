/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common interface for an operation, such as an {@link RpcDefinition} or an {@link ActionDefinition}. Note that this
 * interface is not a {@link DataSchemaNode}, which renders compatibility problematic. Use {@link #toContainerLike()} to
 * get a {@link ContainerLike}, which can serve as a bridge.
 */
@NonNullByDefault
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

    /**
     * Return a {@link ContainerLike} backed by this definition's {@link #getInput()} and {@link #getOutput()}.
     *
     * @return A compatibility {@link ContainerLike}
     */
    default ContainerLikeCompat toContainerLike() {
        return new OperationAsContainer(this);
    }
}
