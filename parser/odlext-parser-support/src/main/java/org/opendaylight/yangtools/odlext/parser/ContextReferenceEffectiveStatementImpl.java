/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;

final class ContextReferenceEffectiveStatementImpl
        extends AbstractIdentityAwareEffectiveStatement<@NonNull ContextReferenceStatement>
        implements ContextReferenceEffectiveStatement {
    ContextReferenceEffectiveStatementImpl(final @NonNull ContextReferenceStatement declared,
            final @NonNull IdentityEffectiveStatement identity,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, identity, substatements);
    }

    @Override
    public IdentityEffectiveStatement contextType() {
        return identity();
    }
}
