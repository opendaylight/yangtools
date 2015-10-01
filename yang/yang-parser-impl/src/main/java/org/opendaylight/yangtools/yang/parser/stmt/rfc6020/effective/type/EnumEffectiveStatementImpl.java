/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumPairBuilder;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.util.EnumPairBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

public final class EnumEffectiveStatementImpl extends EffectiveStatementBase<String, EnumStatement>
    implements EffectiveStatement<String, EnumStatement> {
    private final EnumPair enumPair;

    public EnumEffectiveStatementImpl(final StmtContext<String, EnumStatement, ?> ctx) {
        super(ctx);

        final EnumPairBuilder b = new EnumPairBuilder();
        b.setPath(Utils.getSchemaPath(ctx));
        b.setName(ctx.getStatementArgument());

        for (final EffectiveStatement<?,?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                b.setDescription(((DescriptionEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                b.setReference(((ReferenceEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                b.setStatus(((StatusEffectiveStatementImpl) effectiveStatement).argument());
            }
            if (effectiveStatement instanceof ValueEffectiveStatementImpl) {
                b.setValue(((ValueEffectiveStatementImpl) effectiveStatement).argument());
            }
        }

        enumPair = b.build();
    }

    EnumPair asEnumPair() {
        return enumPair;
    }
}