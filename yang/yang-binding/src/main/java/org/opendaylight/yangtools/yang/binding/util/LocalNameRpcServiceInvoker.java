/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.common.QName;

final class LocalNameRpcServiceInvoker extends AbstractMappedRpcInvoker<String> {
    private LocalNameRpcServiceInvoker(final Map<String, Method> map) {
        super(map);
    }

    static RpcServiceInvoker instanceFor(final Map<QName, Method> qnameToMethod) {
        final Map<String, Method> map = new HashMap<>();
        for (Entry<QName, Method> e : qnameToMethod.entrySet()) {
            map.put(e.getKey().getLocalName(), e.getValue());
        }

        return new LocalNameRpcServiceInvoker(map);
    }

    @Override
    protected String qnameToKey(final QName qname) {
        return qname.getLocalName();
    }
}
