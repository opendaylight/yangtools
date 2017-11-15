/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate;

public final class DeviateStatementRFC6020Support extends AbstractDeviateStatementSupport {
    private static final DeviateStatementRFC6020Support INSTANCE = new DeviateStatementRFC6020Support();

    private DeviateStatementRFC6020Support() {
        // Hidden
    }

    public static DeviateStatementRFC6020Support getInstance() {
        return INSTANCE;
    }
}