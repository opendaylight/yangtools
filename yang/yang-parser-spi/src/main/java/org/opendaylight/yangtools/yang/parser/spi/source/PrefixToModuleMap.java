/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Map-based {@link PrefixToModule} namespace. This class is NOT thread-safe.
 */
public class PrefixToModuleMap implements PrefixToModule {
    private final Map<String, QNameModule> prefixToModuleMap = new HashMap<>();
    private final Map<URI, QNameModule> namespaceToModuleMap = new HashMap<>();
    private final boolean preLinkageMap;

    public PrefixToModuleMap() {
        this(false);
    }

    @Deprecated
    public PrefixToModuleMap(final boolean preLinkageMap) {
        this.preLinkageMap = preLinkageMap;
    }

    public void put(final String prefix, final QNameModule module) {
        prefixToModuleMap.put(prefix, module);
        namespaceToModuleMap.put(module.getNamespace(), module);
    }

    @Override
    public QNameModule get(final String prefix) {
        return prefixToModuleMap.get(prefix);
    }

    @Override
    public QNameModule getByNamespace(final String namespace) throws URISyntaxException {
        return namespaceToModuleMap.get(new URI(namespace));
    }

    @Override
    @Deprecated
    public boolean isPreLinkageMap() {
        return preLinkageMap;
    }
}
