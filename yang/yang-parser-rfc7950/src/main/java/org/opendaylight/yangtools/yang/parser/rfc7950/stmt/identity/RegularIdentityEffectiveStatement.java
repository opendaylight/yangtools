/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.identity;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RegularIdentityEffectiveStatement extends AbstractIdentityEffectiveStatement {
    RegularIdentityEffectiveStatement(final IdentityStatement declared,
            final StmtContext<QName, IdentityStatement, IdentityEffectiveStatement> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, ctx);
    }

    @Override
    public @NonNull QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull Status getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int flags() {
        // TODO Auto-generated method stub
        return 0;
    }

}
