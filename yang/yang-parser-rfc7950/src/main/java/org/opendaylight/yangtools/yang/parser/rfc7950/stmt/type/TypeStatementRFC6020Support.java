/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

public final class TypeStatementRFC6020Support extends AbstractTypeStatementSupport {
    private static final TypeStatementRFC6020Support INSTANCE = new TypeStatementRFC6020Support();

    private TypeStatementRFC6020Support() {
        // Hidden
    }

    public static TypeStatementRFC6020Support getInstance() {
        return INSTANCE;
    }
}