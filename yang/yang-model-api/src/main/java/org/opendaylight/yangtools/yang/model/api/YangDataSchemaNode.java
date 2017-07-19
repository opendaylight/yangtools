/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;

/**
 * Represents 'yang-data' extension statement defined in https://tools.ietf.org/html/rfc8040#section-8
 * This statement must appear as a top-level statement, otherwise it is ignored and does not appear in the final
 * schema context. It must contain exactly one top-level container node (directly or indirectly via a uses statement).
 */
// FIXME: 2.0.0: this interface should live in a separate RFC8040 API artifact.
@Beta
public interface YangDataSchemaNode extends UnknownSchemaNode {

    /**
     * Returns container schema node.
     *
     * @return container schema node
     */
    ContainerSchemaNode getContainer();
}
