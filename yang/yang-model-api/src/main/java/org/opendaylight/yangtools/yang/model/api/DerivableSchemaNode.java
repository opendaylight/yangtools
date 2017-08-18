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
 * Schema Node which may be derived from other schema node
 * using augmentation or uses statement.
 *
 */
public interface DerivableSchemaNode extends DataSchemaNode {

    /**
     * If this node is added by uses, returns original node definition from
     * grouping where it was defined.
     *
     * @return original node definition from grouping if this node is added by
     *         uses, Optional.absent otherwise
     */
    Optional<? extends SchemaNode> getOriginal();

}
