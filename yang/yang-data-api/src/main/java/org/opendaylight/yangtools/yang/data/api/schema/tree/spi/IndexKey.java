/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.annotations.Beta;
import java.util.Collection;

/**
 * Tree node index key interface.
 *
 * @param <V>
 *            index key values type
 */
@Beta
public interface IndexKey<V> {
    /**
     * Get values of index key
     *
     * @return collection of index key values
     */
    Collection<V> getValues();
}
