/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.common.QNameModule;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PrefixToModuleMap implements PrefixToModule {

    private Map<String, QNameModule> prefixToModuleMap = new HashMap<>();

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
    public QNameModule getByNamespace(String namespace) {
        return null;
    }
}
