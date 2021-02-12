/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A simple capture of an {@link EffectiveModelContext} and a list of paths. No further guarantees are made.
 */
@Beta
public abstract class AbstractEffectiveStatementInference extends AbstractEffectiveModelContextProvider
        implements EffectiveStatementInference {
    private final @NonNull List<EffectiveStatement<?, ?>> path;

    protected AbstractEffectiveStatementInference(final @NonNull EffectiveModelContext modelContext,
            final @NonNull ImmutableList<EffectiveStatement<?, ?>> path) {
        super(modelContext);
        this.path = requireNonNull(path);
    }

    protected AbstractEffectiveStatementInference(final @NonNull EffectiveModelContext modelContext,
            final @NonNull List<? extends EffectiveStatement<?, ?>> path) {
        super(modelContext);
        this.path = ImmutableList.copyOf(path);
    }

    @Override
    public final List<EffectiveStatement<?, ?>> statementPath() {
        return path;
    }
}
