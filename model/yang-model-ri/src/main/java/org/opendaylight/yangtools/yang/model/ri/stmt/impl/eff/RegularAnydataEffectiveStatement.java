/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;

/**
 * YANG 1.1 AnyData effective statement implementation.
 */
public final class RegularAnydataEffectiveStatement extends EmptyAnydataEffectiveStatement {
    private final @NonNull Object substatements;

    public RegularAnydataEffectiveStatement(final @NonNull AnydataStatement declared, final QName argument,
            final int flags, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, argument, flags);
        this.substatements = maskList(substatements);
    }

    public RegularAnydataEffectiveStatement(final RegularAnydataEffectiveStatement original, final QName argument,
            final int flags) {
        super(original, argument, flags);
        substatements = original.substatements;
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }
}
