/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.dto;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.binding.RpcService;

public final class RestRpcService implements RpcService {

    private final String namespace;

    public RestRpcService(String namespace){
        Preconditions.checkNotNull(namespace);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
