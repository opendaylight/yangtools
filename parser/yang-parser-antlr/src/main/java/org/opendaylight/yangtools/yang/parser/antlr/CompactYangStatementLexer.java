/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import com.google.common.annotations.Beta;
import org.antlr.v4.runtime.CharStream;
import org.opendaylight.yangtools.yang.parser.antlr.gen.YangStatementLexer;
import org.opendaylight.yangtools.yang.parser.antlr.token.CompactTokenFactory;

/**
 * A {@link YangStatementLexer} backed by more efficient token factory. Exact details are explicitly outside of
 * specification.
 */
@Beta
public final class CompactYangStatementLexer extends YangStatementLexer {
    public CompactYangStatementLexer(final CharStream input) {
        super(input);
        setTokenFactory(CompactTokenFactory.INSTANCE);
    }
}
