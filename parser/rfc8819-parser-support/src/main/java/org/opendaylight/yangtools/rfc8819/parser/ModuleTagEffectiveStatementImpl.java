/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagEffectiveStatement;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.rfc8819.model.api.Tag;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

final class ModuleTagEffectiveStatementImpl extends WithSubstatements<Tag, @NonNull ModuleTagStatement>
        implements ModuleTagEffectiveStatement {
    ModuleTagEffectiveStatementImpl(final @NonNull ModuleTagStatement declared,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }
}
