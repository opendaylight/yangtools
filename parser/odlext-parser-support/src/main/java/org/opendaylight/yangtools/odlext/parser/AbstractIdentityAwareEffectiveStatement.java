/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

abstract class AbstractIdentityAwareEffectiveStatement<D extends DeclaredStatement<QName>>
        extends WithSubstatements<QName, D> {
    private final @NonNull IdentityEffectiveStatement identity;

    AbstractIdentityAwareEffectiveStatement(final @NonNull D declared,
            final @NonNull IdentityEffectiveStatement identity,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
        this.identity = requireNonNull(identity);
    }

    final @NonNull IdentityEffectiveStatement identity() {
        return identity;
    }
}
