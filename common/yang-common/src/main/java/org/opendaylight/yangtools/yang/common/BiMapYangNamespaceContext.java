/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.collect.ImmutableBiMap;

@Deprecated(since = "15.0.0", forRemoval = true)
final class BiMapYangNamespaceContext {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableBiMap<String, QNameModule> mapping = ImmutableBiMap.of();

    @java.io.Serial
    private Object readResolve() {
        return YangNamespaceContext.of(mapping);
    }
}
