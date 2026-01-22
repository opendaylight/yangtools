/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyStatusStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyStatusEffectiveStatement extends DefaultArgument<Status, @NonNull StatusStatement>
        implements StatusEffectiveStatement {
    /*
     * status has low argument cardinality, hence we can reuse them in case declaration does not have any
     * substatements (which is the usual case). Yeah, we could consider an EnumMap, but this is not too bad, either.
     */
    public static final @NonNull EmptyStatusEffectiveStatement CURRENT =
        new EmptyStatusEffectiveStatement(EmptyStatusStatement.CURRENT);
    public static final @NonNull EmptyStatusEffectiveStatement DEPRECATED =
        new EmptyStatusEffectiveStatement(EmptyStatusStatement.DEPRECATED);
    public static final @NonNull EmptyStatusEffectiveStatement OBSOLETE =
        new EmptyStatusEffectiveStatement(EmptyStatusStatement.OBSOLETE);

    public EmptyStatusEffectiveStatement(final @NonNull StatusStatement declared) {
        super(declared);
    }
}
