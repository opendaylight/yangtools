/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * {@link MapNode} which additionally preserves user-supplied ordering. This node represents a data instance of
 * a {@code list} with {@code ordered-by user;} substatement and a {@code key} definition.
 */
public interface SystemMapNode extends MapNode, OrderingAware.System {
    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
