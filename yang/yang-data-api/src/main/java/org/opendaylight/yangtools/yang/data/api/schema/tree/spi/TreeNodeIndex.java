/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Tree node index interface.
 *
 * Tree node index maps index keys to values i.e. normalized nodes. Tree node
 * index provides lookup method to get data associated with specified index key
 * from index. Tree node index should be immutable, however it provides
 * {@link #mutable()} method to get a mutable copy of current tree node index.
 *
 * @param <K>
 *            index key type
 * @param <V>
 *            index value type
 */
@Beta
public interface TreeNodeIndex<K extends IndexKey<?>, V extends NormalizedNode<?, ?>> {
    /**
     * Get data associated with specified index key from index.
     *
     * @param indexKey
     *            index key
     * @return Optional of index data associated with specified index key.
     */
    Optional<V> get(IndexKey<?> indexKey);

    /**
     * Get a mutable copy of tree node index.
     *
     * @return Mutable copy
     */
    MutableTreeNodeIndex<?, ?> mutable();
}
