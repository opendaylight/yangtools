/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7950.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.IdentifierBinding;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * A parser for {@code if-feature} argument into an {@link IfFeatureExpr}.
 */
@NonNullByDefault
public enum IfFeatureArgumentParser {
    /**
     * RFC6020 parser, parsing {@code identifier-ref-arg-str}.
     */
    RFC6020 {
        @Override
        public IfFeatureExpr parseArgument(final CommonStmtCtx stmt, final IdentifierBinding binding,
                final String rawArgument) {
            return IfFeatureExpr.isPresent(binding.parseIdentifierRefArg(stmt, rawArgument));
        }
    },
    /**
     * RFC7950 parser, parsing {@code if-feature-expr}.
     */
    RFC7950 {
        @Override
        public IfFeatureExpr parseArgument(final CommonStmtCtx stmt, final IdentifierBinding binding,
                final String rawArgument) {
            return IfFeaturePredicateParser.parseIfFeatureExpression(stmt, binding, rawArgument);
        }
    };

    /**
     * Parse a statement argument into an {@link IfFeatureExpr} in the context of a particular statement.
     *
     * @param stmt the {@link CommonStmtCtx}
     * @param binding the {@link IdentifierBinding}
     * @param rawArgument the argument
     * @return an IfFeatureExpr
     * @throws SourceException if {@code argument} is not syntactically valid or cannot be resolved
     */
    // FIXME: throws IOExceptiom, YangParserException (perhaps with dedicated type for syntax and resolve)
    public abstract IfFeatureExpr parseArgument(CommonStmtCtx stmt, IdentifierBinding binding, String rawArgument);
}
