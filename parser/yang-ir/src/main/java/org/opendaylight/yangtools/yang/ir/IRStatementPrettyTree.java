/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.PrettyTree;

/**
 * A {@link PrettyTree} producing a YANG snippet from an {@link IRStatement}.
 */
final class IRStatementPrettyTree extends PrettyTree {
    private final IRStatement statement;

    IRStatementPrettyTree(final IRStatement statement) {
        this.statement = requireNonNull(statement);
    }

    @Override
    public void appendTo(final StringBuilder sb, final int depth) {
        statement.toYangFragment(sb);
    }
}
