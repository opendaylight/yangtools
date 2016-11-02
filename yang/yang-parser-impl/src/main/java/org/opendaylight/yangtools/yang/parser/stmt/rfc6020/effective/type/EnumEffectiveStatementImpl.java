/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

public class EnumEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<String, EnumStatement> {
    private final List<UnknownSchemaNode> unknownSchemaNodes;
    private final String name;
    private final Integer declaredValue;

    public EnumEffectiveStatementImpl(final StmtContext<String, EnumStatement, ?> ctx) {
        super(ctx);

        name = ctx.rawStatementArgument();

        final List<UnknownSchemaNode> unknownSchemaNodesInit = new ArrayList<>();
        Integer declaredValueInit = null;
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof ValueEffectiveStatementImpl) {
                declaredValueInit = ((ValueEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownSchemaNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        declaredValue = declaredValueInit;
        unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodesInit);
    }

    public String getName() {
        return name;
    }

    public Integer getDeclaredValue() {
        return declaredValue;
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }
}