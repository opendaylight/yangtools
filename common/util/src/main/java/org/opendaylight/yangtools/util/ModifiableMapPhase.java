/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * A {@link Map} which can be modified and supports efficient conversion to an
 * unmodifiable map. This interface is the logical counterpart to
 * {@link UnmodifiableMapPhase}, but it does not require implementations of
 * {@link #toUnmodifiableMap()} to return an implementation of that interface.
 * The reason for that empty and singleton mappings are efficiently represented
 * as {@link ImmutableMap}, which does not implement
 * {@link UnmodifiableMapPhase}.
 *
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 */
@Beta
public interface ModifiableMapPhase<K, V> extends Map<K, V>, Mutable {
    /**
     * Return an isolated unmodifiable version of this map. Returned object must not allow removal, addition or changing
     * of mappings. Its mappings must match the mappings currently present in this map, but must not be affected by any
     * subsequent changes to this map.
     *
     * @return An unmodifiable version of this map.
     */
    @NonNull Map<K, V> toUnmodifiableMap();
}
