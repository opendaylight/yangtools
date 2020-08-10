/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import com.google.common.annotations.Beta;
import org.antlr.v4.runtime.CharStream;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;

/**
 * A {@link YangStatementLexer} backed by more efficient token factory. Exact details are explicitly outside of
 * specification.
 */
@Beta
public class CompactYangStatementLexer extends YangStatementLexer {
    public CompactYangStatementLexer(final CharStream input) {
        super(input);
        setTokenFactory(CompactTokenFactory.INSTANCE);
    }
}
