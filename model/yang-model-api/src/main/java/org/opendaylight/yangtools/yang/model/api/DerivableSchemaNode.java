/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;

/**
 * Schema Node which may be derived from other schema node using augmentation or uses statement.
 *
 * @deprecated This interface's sole purpose is to aid MD-SAL binding generator, which is now in a position to provide
 *             to implement its logic without this interface.
 */
@Deprecated(since = "7.0.9", forRemoval = true)
public interface DerivableSchemaNode extends DataSchemaNode {
    /**
     * If this node is added by uses, returns original node definition from grouping where it was defined.
     *
     * @return original node definition from grouping if this node is added by
     *         uses, Optional.absent otherwise
     */
    // FIXME: 8.0.0: this should be a type capture as it always matches this node's type
    Optional<? extends SchemaNode> getOriginal();
}
