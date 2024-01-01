/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A base class of an {@link EffectiveModelContext} context reasoning with regards to what logical sequence of
 * {@link EffectiveStatement}s were considered. Implementations of this class may provide additional facts which were
 * derived from the line of reasoning.
 */
@Beta
public interface EffectiveStatementInference extends Immutable {
    /**
     * Return the {@link EffectiveModelContext} against which this inference is made.
     *
     * @return the {@link EffectiveModelContext} against which this inference is made
     */
    @NonNull EffectiveModelContext modelContext();

    /**
     * An {@code Unmodifiable} {@link List} of {@link EffectiveStatement}s, ordered in some meaningful way. Precise
     * semantics of the statement order is clarified by individual {@link EffectiveStatementInference} specializations.
     *
     * @see SchemaTreeInference
     * @return A List of EffectiveStatements
     */
    @NonNull List<? extends @NonNull EffectiveStatement<?, ?>> statementPath();
}
