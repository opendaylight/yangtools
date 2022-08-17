/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2022 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;

/**
 * Map-based {@link PrefixResolver}. This class is NOT thread-safe.
 */
final class HashMapPrefixResolver implements PrefixResolver {
    private final Map<String, QNameModule> prefixToModuleMap = new HashMap<>();

    @Override
    public QNameModule resolvePrefix(final String prefix) {
        return prefixToModuleMap.get(requireNonNull(prefix));
    }

    void put(final String prefix, final QNameModule module) {
        // FIXME: enforce non-nulls
        // FIXME: enforce no duplicates
        prefixToModuleMap.put(prefix, module);
    }
}
