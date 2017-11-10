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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class EffectiveStmtUtils {
    // FIXME: this should reside somewhere in max_elements
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

    public static Optional<ElementCountConstraint> createElementCountConstraint(final EffectiveStatement<?, ?> stmt) {
        final Integer minElements;
        final Optional<Integer> min = stmt.findFirstEffectiveSubstatementArgument(MinElementsEffectiveStatement.class);
        if (min.isPresent()) {
            final Integer m = min.get();
            minElements = m > 0 ? m : null;
        } else {
            minElements = null;
        }

        final Integer maxElements;
        final String max = stmt.findFirstEffectiveSubstatementArgument(MaxElementsEffectiveStatement.class)
                .orElse(UNBOUNDED_STR);
        if (!UNBOUNDED_STR.equals(max)) {
            final Integer m = Integer.valueOf(max);
            maxElements = m < Integer.MAX_VALUE ? m : null;
        } else {
            maxElements = null;
        }

        return ElementCountConstraint.forNullable(minElements, maxElements);
    }
}
