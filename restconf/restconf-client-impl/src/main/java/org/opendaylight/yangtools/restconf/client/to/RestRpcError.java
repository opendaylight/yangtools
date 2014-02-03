/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

import org.opendaylight.yangtools.yang.common.RpcError;

public class RestRpcError implements RpcError {

    private final ErrorSeverity severity;
    private final String tag;
    private final String applicationTag;
    private final String message;
    private final String info;
    private final Throwable cause;
    private final ErrorType errorType;

    public RestRpcError(ErrorSeverity severity,  ErrorType type,String tag,String applicationTag,String message,String info,Throwable cause){
        this.severity = severity;
        this.tag = tag;
        this.applicationTag = applicationTag;
        this.message = message;
        this.info = info;
        this.cause = cause;
        this.errorType = type;
    }
    public RestRpcError(ErrorSeverity severity, ErrorType type, String message,Throwable cause){
        this.severity = severity;
        this.message = message;
        this.cause = cause;
        this.errorType = type;
        this.info = "";
        this.applicationTag = "";
        this.tag = "";
    }
    @Override
    public ErrorSeverity getSeverity() {
        return this.severity;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    @Override
    public String getApplicationTag() {
        return this.applicationTag;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public ErrorType getErrorType() {
        return this.errorType;
    }
}
