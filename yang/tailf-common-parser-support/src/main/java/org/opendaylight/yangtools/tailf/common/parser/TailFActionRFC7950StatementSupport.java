/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.parser;

import com.google.common.annotations.Beta;

@Beta
public final class TailFActionRFC7950StatementSupport extends AbstractTailFActionStatementSupport {
    private static final TailFActionRFC7950StatementSupport INSTANCE = new TailFActionRFC7950StatementSupport();

    private TailFActionRFC7950StatementSupport() {
        super();
    }

    public static TailFActionRFC7950StatementSupport getInstance() {
        return INSTANCE;
    }
}
