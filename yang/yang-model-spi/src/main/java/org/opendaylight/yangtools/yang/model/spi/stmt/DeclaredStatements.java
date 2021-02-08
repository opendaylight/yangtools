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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;

/**
 * Static entry point to instantiating {@link DeclaredStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class DeclaredStatements {
    private DeclaredStatements() {
        // Hidden on purpose
    }

    public static ActionStatement createAction(final QName argument) {
        return new EmptyActionStatement(argument);
    }

    public static ActionStatement createAction(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createAction(argument) : new RegularActionStatement(argument, substatements);
    }
}
