/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDeviateStatement;

/**
 * Static entry point to enriching {@link DeclaredStatement}s covered in the {@code RFC7950} metamodel with
 * {@link DeclarationReference}s.
 */
@Beta
@NonNullByDefault
public final class DeclarationReferenceDecorators {
    private DeclarationReferenceDecorators() {
        // Hidden on purpose
    }

    public static DeviateStatement decorateDeviate(final DeviateStatement delegate, final DeclarationReference ref) {
        return new RefDeviateStatement(delegate, ref);
    }
}
