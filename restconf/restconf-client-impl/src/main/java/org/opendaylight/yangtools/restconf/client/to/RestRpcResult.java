/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

public final class RestRpcResult implements RpcResult {

    private boolean succeeded;
    private Object result;
    private Collection<RpcError> errors;

    public RestRpcResult(boolean succeeded,Object result,Collection<RpcError> errors){
        this.succeeded = succeeded;
        this.result = result;
        this.errors = errors;
    }
    public RestRpcResult(boolean succeeded,Object result){
        this.succeeded = succeeded;
        this.result = result;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public Object getResult() {
        return this.result;
    }

    @Override
    public Collection<RpcError> getErrors() {
        return this.errors;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

}
