/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Structural containers are special in that they appear when implied by child
 * nodes and disappear whenever they are empty.
 *
 * FIXME: BUG-2399: right now this behaves just like a presence container
 */
final class StructuralContainerModificationStrategy extends AbstractContainerModificationStrategy {
    StructuralContainerModificationStrategy(final ContainerSchemaNode schemaNode) {
        super(schemaNode);
    }
}
