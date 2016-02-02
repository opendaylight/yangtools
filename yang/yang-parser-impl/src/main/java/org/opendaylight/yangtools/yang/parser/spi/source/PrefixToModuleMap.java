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
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class PrefixToModuleMap implements PrefixToModule {

    private Map<String, QNameModule> prefixToModuleMap = new HashMap<>();
    private final boolean preLinkageMap;

    public PrefixToModuleMap() {
        this(false);
    }

    public PrefixToModuleMap(boolean preLinkageMap) {
        this.preLinkageMap = preLinkageMap;
    }

    public void put(String prefix, QNameModule qNameModule) {
        prefixToModuleMap.put(prefix, qNameModule);
    }

    @Nullable
    @Override
    public QNameModule get(String prefix) {
        return prefixToModuleMap.get(prefix);
    }

    @Nullable
    @Override
    public QNameModule getByNamespace(String namespace) throws URISyntaxException {
        URI ns = new URI(namespace);
        for (Entry<String, QNameModule> entry : prefixToModuleMap.entrySet()) {
            QNameModule qnameModule = entry.getValue();
            if(qnameModule.getNamespace().equals(ns)) {
                return qnameModule;
            }
        }
        return null;
    }

    @Override
    public boolean isPreLinkageMap() {
        return preLinkageMap;
    }
}
