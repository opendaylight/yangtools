/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

@Deprecated(since = "15.0.0", forRemoval = true)
final class StmtNamespaceContext {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final ImmutableBiMap<QNameModule, String> moduleToPrefix = ImmutableBiMap.of();
    private final ImmutableMap<String, QNameModule> prefixToModule = ImmutableMap.of();

    @java.io.Serial
    private Object readResolve() {
        final var map = new HashMap<String, QNameModule>();
        map.putAll(prefixToModule);
        map.putAll(moduleToPrefix.inverse());
        return YangNamespaceContext.of(map);
    }
}
