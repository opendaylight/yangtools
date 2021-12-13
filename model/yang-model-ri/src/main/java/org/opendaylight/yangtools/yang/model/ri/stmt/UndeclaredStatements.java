/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredCaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredInputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.UndeclaredOutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;

/**
 * Static entry point to instantiating undeclared {@link EffectiveStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class UndeclaredStatements {
    private UndeclaredStatements() {
        // Hidden on purpose
    }

    public static CaseEffectiveStatement createCase(final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new UndeclaredCaseEffectiveStatement(substatements, argument, flags);
    }

    public static InputEffectiveStatement createInput(final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
            throws SubstatementIndexingException {
        return new UndeclaredInputEffectiveStatement(substatements, argument, flags);
    }

    public static OutputEffectiveStatement createOutput(final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements)
            throws SubstatementIndexingException {
        return new UndeclaredOutputEffectiveStatement(substatements, argument, flags);
    }
}
