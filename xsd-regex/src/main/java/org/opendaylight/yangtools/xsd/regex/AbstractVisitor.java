/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.xsd.regex;

import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpBaseVisitor;
import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpParser.CharClassExprContext;
import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpParser.NegCharGroupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractVisitor extends XSDRegExpBaseVisitor<StringBuilder> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVisitor.class);

    final StringBuilder sb = new StringBuilder();

    @Override
    public final StringBuilder visitCharClassExpr(final CharClassExprContext ctx) {
        sb.append('[');
        super.visitCharClassExpr(ctx);
        return sb.append(']');
    }

    @Override
    public final StringBuilder visitNegCharGroup(final NegCharGroupContext ctx) {
        sb.append('^');
        super.visitNegCharGroup(ctx);
        return sb.append(']');
    }
}
