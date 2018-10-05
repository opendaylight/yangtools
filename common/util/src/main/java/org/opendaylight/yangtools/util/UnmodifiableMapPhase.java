/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A {@link Map} which cannot be modified and supports efficient conversion to a {@link ModifiableMapPhase}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@Beta
public interface UnmodifiableMapPhase<K, V> extends Map<K, V>, Immutable {
    /**
     * Return an isolated modifiable version of this map. Its mappings must match the mappings present in this map. Any
     * modification of the returned map must not be affect the contents of this map.
     *
     * @return An modifiable version of this map.
     */
    @Nonnull ModifiableMapPhase<K, V> toModifiableMap();
}
