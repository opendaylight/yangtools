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
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link EffectiveStatementInference}s with its attached {@link EffectiveModelContext}.
 *
 * @param <T> constituent {@link EffectiveStatement} type
 */
@Beta
public abstract class AbstractEffectiveStatementInference<T extends EffectiveStatement<?, ?>>
        implements EffectiveStatementInference {
    private final @NonNull EffectiveModelContext modelContext;

    protected AbstractEffectiveStatementInference(final @NonNull EffectiveModelContext modelContext) {
        this.modelContext = requireNonNull(modelContext);
    }

    @Override
    public final EffectiveModelContext modelContext() {
        return modelContext;
    }

    @Override
    public abstract List<T> statementPath();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("modelContext", modelContext);
    }

    /**
     * A simple capture of an {@link AbstractEffectiveStatementInference} and a list of {@link EffectiveStatement}s. No
     * further guarantees are made.
     *
     * @param <T> constituent {@link EffectiveStatement} type
     */
    @Beta
    public abstract static class WithPath<T extends EffectiveStatement<?, ?>>
            extends AbstractEffectiveStatementInference<T> {
        private final @NonNull List<T> path;

        protected WithPath(final @NonNull EffectiveModelContext modelContext, final @NonNull ImmutableList<T> path) {
            super(modelContext);
            this.path = requireNonNull(path);
        }

        protected WithPath(final @NonNull EffectiveModelContext modelContext, final @NonNull List<? extends T> path) {
            super(modelContext);
            this.path = ImmutableList.copyOf(path);
        }

        @Override
        public final List<T> statementPath() {
            return path;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return super.addToStringAttributes(helper).add("path", path);
        }
    }
}
