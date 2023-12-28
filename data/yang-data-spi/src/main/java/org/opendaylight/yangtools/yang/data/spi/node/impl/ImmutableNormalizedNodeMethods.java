/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

/**
 * Various utility methods for implementing NormalizedNode contracts.
 */
final class ImmutableNormalizedNodeMethods {
    private ImmutableNormalizedNodeMethods() {
        // Hidden on purpose
    }

    static boolean bodyEquals(final DataContainerNode thisInstance, final DataContainerNode other) {
        if (thisInstance.size() != other.size()) {
            return false;
        }
        for (var child : thisInstance.body()) {
            if (!child.equals(other.childByArg(child.name()))) {
                return false;
            }
        }
        return true;
    }
}
