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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QNameModule;

@NotThreadSafe
public class PrefixToModuleMap implements PrefixToModule {
    private final Map<String, QNameModule> prefixToModuleMap = new HashMap<>();
    private final Map<URI, QNameModule> namespaceToModuleMap = new HashMap<>();
    private final boolean preLinkageMap;

    public PrefixToModuleMap() {
        this(false);
    }

    public PrefixToModuleMap(boolean preLinkageMap) {
        this.preLinkageMap = preLinkageMap;
    }

    public void put(String prefix, QNameModule module) {
        prefixToModuleMap.put(prefix, module);
        namespaceToModuleMap.put(module.getNamespace(), module);
    }

    @Nullable
    @Override
    public QNameModule get(@Nonnull String prefix) {
        return prefixToModuleMap.get(prefix);
    }

    @Nullable
    @Override
    public QNameModule getByNamespace(String namespace) throws URISyntaxException {
        return namespaceToModuleMap.get(new URI(namespace));
    }

    @Override
    public boolean isPreLinkageMap() {
        return preLinkageMap;
    }
}
