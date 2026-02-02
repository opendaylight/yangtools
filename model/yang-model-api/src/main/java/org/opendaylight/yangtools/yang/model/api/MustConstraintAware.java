/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Mix-in interface for nodes which can define must constraints.
 */
public interface MustConstraintAware {
    /**
     * Specifies the rules which the node which contains {@code must} YANG substatement has to match.
     *
     * @return collection of {@code MustDefinition} (XPath) instances which represents the concrete data constraints
     */
    @NonNull Collection<? extends @NonNull MustDefinition> getMustConstraints();

    /**
     * Bridge between {@link EffectiveStatement} and {@link ActionNodeContainer}.
     *
     * @param <E> Type of equivalent {@link EffectiveStatement}.
     * @since 15.0.0
     */
    interface Mixin<E extends EffectiveStatement<?, ?>> extends EffectiveStatementEquivalent<E>, MustConstraintAware {
        @Override
        default Collection<? extends MustDefinition> getMustConstraints() {
            return asEffectiveStatement().filterEffectiveStatements(MustDefinition.class);
        }
    }
}
