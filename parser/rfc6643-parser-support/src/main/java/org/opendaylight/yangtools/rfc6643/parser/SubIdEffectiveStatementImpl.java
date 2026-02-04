/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.SubIdStatement;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

final class SubIdEffectiveStatementImpl extends WithSubstatements<Uint32, @NonNull SubIdStatement>
        implements SubIdEffectiveStatement {
    SubIdEffectiveStatementImpl(final @NonNull SubIdStatement declared,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }
}
