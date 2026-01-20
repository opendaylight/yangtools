/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.odlext.model.api.LegacyAugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.LegacyAugmentIdentifierStatement;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

@NonNullByDefault
final class LegacyAugmentIdentifierEffectiveStatementImpl
        extends WithSubstatements<Unqualified, LegacyAugmentIdentifierStatement>
        implements LegacyAugmentIdentifierEffectiveStatement {
    LegacyAugmentIdentifierEffectiveStatementImpl(final LegacyAugmentIdentifierStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }
}
