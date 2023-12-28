/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import com.google.common.annotations.Beta;
import java.util.Map;

@Beta
public interface CloneableMap<K, V> extends Map<K, V> {
    /**
     * Create a clone of this map's contents. This does not include the actual
     * keys and values, just the internal map state.
     *
     * @return An isolated, writable map.
     */
    Map<K, V> createMutableClone();
}
