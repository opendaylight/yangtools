/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureSchemaNode;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DataNodeContainerMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin.WithStatus;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.MustConstraintMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.UnknownSchemaNodeMixin;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class StructureEffectiveStatementImpl
        extends UnknownEffectiveStatementBase<QName, StructureStatement>
        implements StructureEffectiveStatement, StructureSchemaNode, WithStatus<QName, StructureStatement>,
                   MustConstraintMixin<QName, StructureStatement>, UnknownSchemaNodeMixin<QName, StructureStatement>,
                   DataNodeContainerMixin<QName, StructureStatement> {
   StructureEffectiveStatementImpl(final StmtContext<QName, StructureStatement, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(ctx, substatements);
    }

    @Override
    public @NonNull QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull SchemaPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int flags() {
        // TODO Auto-generated method stub
        return 0;
    }

}
