/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUndeclaredCaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUndeclaredInputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUndeclaredOutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUndeclaredCaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUndeclaredInputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUndeclaredOutputStatement;

/**
 * Static entry point to instantiating {@link DeclaredStatements} covered in the {@code RFC7950} metamodel which are
 * not really declared, but rather implicit.
 */
@Beta
@NonNullByDefault
public final class ImplicitStatements {
    private ImplicitStatements() {
        // Hidden on purpose
    }

    public static CaseStatement createCase(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyUndeclaredCaseStatement(argument)
            : new RegularUndeclaredCaseStatement(argument, substatements);
    }

    public static InputStatement createInput(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyUndeclaredInputStatement(argument)
            : new RegularUndeclaredInputStatement(argument, substatements);
    }

    public static OutputStatement createOutput(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyUndeclaredOutputStatement(argument)
            : new RegularUndeclaredOutputStatement(argument, substatements);
    }
}
