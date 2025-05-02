/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;

public final class RegularLeafListEffectiveStatement extends AbstractNonEmptyLeafListEffectiveStatement {
    private final @NonNull ImmutableSet<String> defaults;

    public RegularLeafListEffectiveStatement(final LeafListStatement declared, final QName argument, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final ImmutableSet<String> defaults,
            final ElementCountMatcher elementCountMatcher) {
        super(declared, argument, flags, substatements, elementCountMatcher);
        this.defaults = requireNonNull(defaults);
    }

    public RegularLeafListEffectiveStatement(final RegularLeafListEffectiveStatement originalEffective,
            final QName argument, final int flags) {
        super(originalEffective, argument, flags);
        defaults = originalEffective.defaults;
    }

    @Override
    public ImmutableSet<String> getDefaults() {
        return defaults;
    }
}
