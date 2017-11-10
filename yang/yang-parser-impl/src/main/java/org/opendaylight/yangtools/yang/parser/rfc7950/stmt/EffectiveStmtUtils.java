/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint.Builder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class EffectiveStmtUtils {
    // FIXME: this really should live by MaxElements{Effective}Statement
    private static final String UNBOUNDED_STR = "unbounded";

    private EffectiveStmtUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static SourceException createNameCollisionSourceException(final StmtContext<?, ?, ?> ctx,
            final EffectiveStatement<?, ?> effectiveStatement) {
        return new SourceException(ctx.getStatementSourceReference(),
            "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared.",
            ctx.getRoot().getStatementArgument(),
            effectiveStatement.argument(),
            effectiveStatement.argument());
    }

    public static Optional<ElementCountConstraint> createElementCountConstraint(final StmtContext<?, ?, ?> ctx) {
        final Integer minElements;
        final StmtContext<Integer, ?, ?> minElementsStmt = StmtContextUtils.findFirstEffectiveSubstatement(ctx,
            MinElementsStatement.class);
        if (minElementsStmt != null) {
            final Integer m = minElementsStmt.getStatementArgument();
            minElements = m > 0 ? m : null;
        } else {
            minElements = null;
        }

        final StmtContext<String, ?, ?> maxElementsStmt = StmtContextUtils.findFirstEffectiveSubstatement(ctx,
            MaxElementsStatement.class);
        final String maxElementsArg = maxElementsStmt == null ? UNBOUNDED_STR : maxElementsStmt.getStatementArgument();
        final Integer maxElements;
        if (!UNBOUNDED_STR.equals(maxElementsArg)) {
            final Integer m = Integer.valueOf(maxElementsArg);
            maxElements = m < Integer.MAX_VALUE ? m : null;
        } else {
            maxElements = null;
        }

        if (minElements == null && maxElements == null) {
            return Optional.empty();
        }

        final Builder builder = ElementCountConstraint.builder();
        if (minElements != null) {
            builder.setMinElements(minElements);
        }
        if (maxElements != null) {
            builder.setMaxElements(maxElements);
        }

        final ElementCountConstraint constraint;
        try {
            constraint = builder.build();
        } catch (IllegalArgumentException e) {
            throw new InferenceException(ctx.getStatementSourceReference(),
                "Illegal min/max elements constraints %s/%s", minElements, maxElements, e);
        }

        return Optional.of(constraint);
    }
}
