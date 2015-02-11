/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;

/**
 *
 * Thrown to indicate error in YANG model source.
 *
 */
public class SourceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final StatementSourceReference sourceRef;

    public SourceException(String message,StatementSourceReference source) {
        super(Preconditions.checkNotNull(message));
        sourceRef = Preconditions.checkNotNull(source);
    }

    public SourceException(String message,StatementSourceReference source, Throwable cause) {
        super(message,cause);
        sourceRef = Preconditions.checkNotNull(source);
    }

    public StatementSourceReference getSourceReference() {
        return sourceRef;
    }

}
