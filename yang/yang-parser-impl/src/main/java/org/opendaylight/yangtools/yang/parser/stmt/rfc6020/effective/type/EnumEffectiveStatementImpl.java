/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;

public class EnumEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, EnumStatement> implements EnumPair {
    private final String name;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private Integer value;

    public EnumEffectiveStatementImpl(final StmtContext<String, EnumStatement, ?> ctx) {
        super(ctx);

        name = ctx.rawStatementArgument();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ValueEffectiveStatementImpl) {
                value = ((ValueEffectiveStatementImpl) effectiveStatement).argument();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}