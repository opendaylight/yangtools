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

public class RestRpcResult implements RpcResult {

    private boolean succeeded;
    private Object result;
    private Collection<RpcError> errors;

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public Collection<RpcError> getErrors() {
        return null;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setErrors(Collection<RpcError> errors) {
        this.errors = errors;
    }
}
