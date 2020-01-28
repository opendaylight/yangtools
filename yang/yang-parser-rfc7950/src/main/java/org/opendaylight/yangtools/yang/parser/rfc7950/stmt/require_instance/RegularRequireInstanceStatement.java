/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.require_instance;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractBooleanDeclaredStatement;

final class RegularRequireInstanceStatement extends AbstractBooleanDeclaredStatement
        implements RequireInstanceStatement {
    private final @NonNull Object substatements;

    RegularRequireInstanceStatement(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(argument);
        this.substatements = maskList(substatements);
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}
