/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointEffectiveStatement;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultWithArgument.WithSubstatements;

final class MountPointEffectiveStatementImpl extends WithSubstatements<MountPointLabel, @NonNull MountPointStatement>
        implements MountPointEffectiveStatement {
    MountPointEffectiveStatementImpl(final @NonNull MountPointStatement declared, final MountPointLabel argument,
            final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, argument, substatements);
    }
}
