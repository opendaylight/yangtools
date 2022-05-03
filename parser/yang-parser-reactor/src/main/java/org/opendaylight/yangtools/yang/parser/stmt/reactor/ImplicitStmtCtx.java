/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * A statement which has not been declared, but exists in the statement hierarchy through being an implicit intermediate
 * statement -- like a {@code case} created by a {@code leaf} inside a {@code choice}.
 *
 * <p>
 * That the contract of this class requires the caller to add child effective statement, if that does not happen, you
 * get to keep the pieces.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
final class ImplicitStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends UndeclaredStmtCtx<A, D, E> {
    private boolean checkedSubstatement;

    // Exposed for AbstractResumedStatement.createUndeclared()
    ImplicitStmtCtx(final StatementContextBase<?, ?, ?> parent, final StatementSupport<A, D, E> support,
            final String rawArgument) {
        super(parent, support, rawArgument);
    }

    @Override
    public boolean isSupportedToBuildEffective() {
        // The availability of this statement depends on its first substatement, added by the sole user of this class.
        // We do not really have a reasonable lifecycle hook (yet?), so here we deactivate just before the state could
        // be leaked.
        if (!checkedSubstatement) {
            final var substatements = effectiveSubstatements();
            verify(!substatements.isEmpty(), "Unexpected empty substatements in %s", this);
            final var substatement = substatements.iterator().next();
            // Note: isSupportedByFeatures() is implemented as a walk towards root and we are walking in the opposite
            //       direction. This check therefore must never be checked as part of isSupportedByFeatures() -- for
            //       that reason we first in our direction (which may involve recursive calls to this method in a
            //       substatement)
            if (!substatement.isSupportedToBuildEffective() || !substatement.isSupportedByFeatures()) {
                setUnsupported();
            }
            checkedSubstatement = true;
        }
        return super.isSupportedToBuildEffective();
    }
}
