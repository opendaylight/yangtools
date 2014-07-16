/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

public class DocumentedException extends Exception implements RpcError {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final ErrorType errorType;
    private final ErrorSeverity errorSeverity;
    private final String applicationTag;
    private final String info;
    private final String tag;

    public DocumentedException(final String message, final Throwable cause, final ErrorType type, final ErrorSeverity severity, final String tag, final String appTag, final String info) {
        super(message, cause);
        this.errorType =  type;
        this.errorSeverity = severity;
        this.tag = tag;
        this.applicationTag =  appTag;
        this.info = info;
    }

    public DocumentedException(final String message, final ErrorType type, final ErrorSeverity severity, final String tag, final String appTag, final String info) {
        this(message,null,type,severity,tag,appTag,info);
    }

    public DocumentedException(final Throwable cause, final RpcError base) {
        super(base.getMessage(),cause);
        this.errorType = base.getErrorType();
        this.errorSeverity = base.getSeverity();
        this.tag = base.getTag();
        this.applicationTag = base.getApplicationTag();
        this.info = base.getInfo();
    }

    public DocumentedException(final RpcError base) {
        super(base.getMessage(),base.getCause());
        this.errorType = base.getErrorType();
        this.errorSeverity = base.getSeverity();
        this.tag = base.getTag();
        this.applicationTag = base.getApplicationTag();
        this.info = base.getInfo();
    }

    @Override
    public String getApplicationTag() {
        return applicationTag;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public ErrorSeverity getSeverity() {
        return errorSeverity;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public String getTag() {
        return tag;
    }

}
