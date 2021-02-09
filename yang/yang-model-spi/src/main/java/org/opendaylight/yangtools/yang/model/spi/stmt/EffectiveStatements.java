/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.EmptyArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.EmptyBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.EmptyWhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.RegularArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.RegularBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff.RegularWhenEffectiveStatement;

/**
 * Static entry point to instantiating {@link EffectiveStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class EffectiveStatements {
    private EffectiveStatements() {
        // Hidden on purpose
    }

    public static ArgumentEffectiveStatement createArgument(final ArgumentStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyArgumentEffectiveStatement(declared)
            : new RegularArgumentEffectiveStatement(declared, substatements);
    }

    public static BaseEffectiveStatement createBase(final BaseStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBaseEffectiveStatement(declared)
            : new RegularBaseEffectiveStatement(declared, substatements);
    }

    public static WhenEffectiveStatement createWhen(final WhenStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyWhenEffectiveStatement(declared)
            : new RegularWhenEffectiveStatement(declared, substatements);
    }
}
