/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointEffectiveStatement;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractEffectiveUnknownSchmemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

@NonNullByDefault
final class MountPointEffectiveStatementImpl
        extends AbstractEffectiveUnknownSchmemaNode<MountPointLabel, MountPointStatement>
        implements MountPointEffectiveStatement, MountPointSchemaNode {
    MountPointEffectiveStatementImpl(final Current<MountPointLabel, MountPointStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(stmt.declared(), stmt.getArgument(), stmt.history(), substatements);
    }

    @Override
    public QName getQName() {
        return argument().qname();
    }

    @Override
    public MountPointEffectiveStatement asEffectiveStatement() {
        return this;
    }
}