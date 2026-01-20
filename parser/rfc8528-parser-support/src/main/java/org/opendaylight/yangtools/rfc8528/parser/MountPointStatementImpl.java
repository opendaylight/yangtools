/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement;

@NonNullByDefault
final class MountPointStatementImpl extends AbstractDeclaredStatement<MountPointLabel> implements MountPointStatement {
    private final MountPointLabel argument;
    private final Object substatements;

    MountPointStatementImpl(final MountPointLabel argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        this.argument = requireNonNull(argument);
        this.substatements = maskList(substatements);
    }

    @Override
    public MountPointLabel argument() {
        return argument;
    }

    @Override
    public ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}