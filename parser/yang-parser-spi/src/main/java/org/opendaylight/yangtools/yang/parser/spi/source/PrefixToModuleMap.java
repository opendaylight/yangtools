/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Map-based {@link PrefixToModule} namespace. This class is NOT thread-safe.
 */
public class PrefixToModuleMap extends PrefixToModule {
    private final Map<XMLNamespace, QNameModule> namespaceToModuleMap = new HashMap<>();
    private final Map<String, QNameModule> prefixToModuleMap = new HashMap<>();

    public void put(final String prefix, final QNameModule module) {
        prefixToModuleMap.put(prefix, module);
        namespaceToModuleMap.put(module.getNamespace(), module);
    }

    @Override
    public QNameModule get(final String prefix) {
        return prefixToModuleMap.get(prefix);
    }

    @Override
    public QNameModule getByNamespace(final String namespace) {
        return namespaceToModuleMap.get(XMLNamespace.of(namespace));
    }
}
