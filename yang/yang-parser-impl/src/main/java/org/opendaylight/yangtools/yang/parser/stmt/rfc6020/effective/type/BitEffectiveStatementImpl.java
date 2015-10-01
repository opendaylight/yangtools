/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.util.BitBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PositionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;

public class BitEffectiveStatementImpl extends EffectiveStatementBase<QName, BitStatement> {
    private final BitBuilder builder;
    private boolean havePosition;

    public BitEffectiveStatementImpl(final StmtContext<QName, BitStatement, ?> ctx) {
        super(ctx);

        this.builder = new BitBuilder();
        builder.setPath(Utils.getSchemaPath(ctx));

        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                builder.setDescription(((DescriptionEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                builder.setReference(((ReferenceEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                builder.setStatus(((StatusEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof PositionEffectiveStatementImpl) {
                builder.setPosition(((PositionEffectiveStatementImpl) effectiveStatement).argument());
                havePosition = true;
            }

            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownSchemaNodes.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        builder.setUnknownSchemaNodes(unknownSchemaNodes);
    }

    public Bit asBit(final Long defaultPosition) {
        if (havePosition) {
            builder.setPosition(defaultPosition);
        }

        return builder.build();
    }
}
