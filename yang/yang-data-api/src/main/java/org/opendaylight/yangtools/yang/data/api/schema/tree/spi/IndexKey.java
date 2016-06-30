/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Tree node index key interface. Index key is formed from map of yang instance
 * identifiers of leafs to values of these leafs. Retrieving of values from
 * TreeNodeIndexes is performed on the basis of equality of index keys.
 *
 * @param <V>
 *            value type
 */
@Beta
public interface IndexKey<V> {
    /**
     * Get index key value i.e. mapping of yang instance identifiers to values.
     *
     * @return mapping of yang instance identifiers to values
     */
    Map<YangInstanceIdentifier, V> getValue();
}
