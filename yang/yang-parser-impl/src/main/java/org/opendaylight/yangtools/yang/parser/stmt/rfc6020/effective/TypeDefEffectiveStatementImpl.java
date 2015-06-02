/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class TypeDefEffectiveStatementImpl extends EffectiveStatementBase<QName, TypedefStatement> implements
        TypeDefinition {

    private final QName qName;
    private final SchemaPath path;

    private String description;
    private String reference;

    private Status status;

    public TypeDefEffectiveStatementImpl(StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        qName = ctx.getStatementArgument();
        path = Utils.getSchemaPath(ctx);

        for (final EffectiveStatement<?,?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) effectiveStatement).argument();
            }
        }

        // TODO get BaseType
    }

    @Override
    public TypeDefinition<?> getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public SchemaPath getPath() {
        return path;
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
