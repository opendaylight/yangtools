/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A read-only view of a {@link DataTree}. This provides access to MVCC access methods, but unlike {@link DataTree},
 * it does not expose methods to affect internal state.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface ReadOnlyDataTree {
    /**
     * Get the root path of this data tree.
     *
     * @return The tree's root path.
     */
    YangInstanceIdentifier getRootPath();

    /**
     * Take a read-only point-in-time snapshot of the tree.
     *
     * @return Data tree snapshot.
     */
    DataTreeSnapshot takeSnapshot();
}
