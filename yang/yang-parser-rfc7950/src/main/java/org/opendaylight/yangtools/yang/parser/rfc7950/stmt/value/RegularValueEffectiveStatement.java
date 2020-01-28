/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.value;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;

final class RegularValueEffectiveStatement extends AbstractValueEffectiveStatement {
    private final @NonNull Object substatements;

    RegularValueEffectiveStatement(final ValueStatement declared,
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = maskList(substatements);
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }
}
