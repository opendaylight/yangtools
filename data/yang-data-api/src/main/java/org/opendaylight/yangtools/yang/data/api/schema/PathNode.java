/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link NormalizedNode} which is addressable through a {@link PathArgument}.
 */
// FIXME: seal this interface
public interface PathNode<T extends PathArgument> extends NormalizedNode {
    /**
     * This node's {@link PathArgument} component.
     *
     * @return A PathArgument.
     */
    @NonNull T pathArgument();
}
