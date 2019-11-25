/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractEffectiveMustConstraintAwareSimpleDataNodeContainer<D extends DeclaredStatement<QName>>
        extends AbstractEffectiveSimpleDataNodeContainer<D> implements MustConstraintAware {
    private static final VarHandle MUST_CONSTRAINTS;

    static {
        try {
            MUST_CONSTRAINTS = MethodHandles.lookup().findVarHandle(
                AbstractEffectiveMustConstraintAwareSimpleDataNodeContainer.class, "mustConstraints",
                ImmutableSet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private volatile ImmutableSet<MustDefinition> mustConstraints;

    protected AbstractEffectiveMustConstraintAwareSimpleDataNodeContainer(final StmtContext<QName, D, ?> ctx) {
        super(ctx);
    }

    @Override
    public final ImmutableSet<MustDefinition> getMustConstraints() {
        return derivedSet(MUST_CONSTRAINTS, MustDefinition.class);
    }
}
