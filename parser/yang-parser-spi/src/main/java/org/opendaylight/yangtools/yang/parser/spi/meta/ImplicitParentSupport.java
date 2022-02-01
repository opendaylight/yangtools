/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * An intermediate support for creating implicit parents statements. This is used for instantiating {@code case}
 * statements implied by {@code container}s and similar in a {@code choice} statement.
 */
@Beta
public interface ImplicitParentSupport {
    /**
     * Wrap a child {@link EffectiveStatement} defined in specified parent {@link NamespaceStmtCtx} with an intermediate
     * statement.
     *
     * @param parentCtx Parent context
     * @param child Child statement
     * @return Either the child statement, or a statement containing it as its sole child
     */
    @NonNull EffectiveStatement<?, ?> wrapChild(@NonNull NamespaceStmtCtx parentCtx,
        @NonNull EffectiveStatement<?, ?> child);
}
