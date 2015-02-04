/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafref.parser;

public class LeafRefPathSyntaxErrorException extends LeafRefYangSyntaxErrorException{

    private static final long serialVersionUID = 1L;

    public LeafRefPathSyntaxErrorException(final String module, final int line, final int charPositionInLine,
            final String message) {
        super(module, line, charPositionInLine, message, null);
    }

    public LeafRefPathSyntaxErrorException(final String module, final int line, final int charPositionInLine,
            final String message, final Throwable cause) {
        super(module,line,charPositionInLine,message,cause);
    }
}
