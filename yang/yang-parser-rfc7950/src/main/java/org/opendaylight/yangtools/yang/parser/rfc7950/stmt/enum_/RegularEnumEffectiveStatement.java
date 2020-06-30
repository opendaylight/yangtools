/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

final class RegularEnumEffectiveStatement extends WithSubstatements<String, EnumStatement>
        implements EnumEffectiveStatement, DocumentedNodeMixin<String, EnumStatement>, WithStatus {
    RegularEnumEffectiveStatement(final EnumStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public Status getStatus() {
        return findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }
}
