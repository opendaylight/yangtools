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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PositionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;

public class BitEffectiveStatementImpl extends EffectiveStatementBase<QName, BitStatement> implements
        BitsTypeDefinition.Bit {

    private final QName qName;
    private final SchemaPath schemaPath;
    private Long position;
    private String description;
    private String reference;
    private Status status;
    private List<UnknownSchemaNode> unknownSchemaNodes;

    public BitEffectiveStatementImpl(StmtContext<QName, BitStatement, ?> ctx) {
        super(ctx);

        List<UnknownSchemaNode> unknownSchemaNodesInit = new ArrayList<>();

        qName = ctx.getStatementArgument();
        schemaPath = Utils.getSchemaPath(ctx);

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
            if (effectiveStatement instanceof PositionEffectiveStatementImpl) {
                position = ((PositionEffectiveStatementImpl) effectiveStatement).argument();
            }

            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownSchemaNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
        }

        unknownSchemaNodes = ImmutableList.copyOf(unknownSchemaNodesInit);
    }

    @Override
    public Long getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return qName.getLocalName();
    }

    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + qName.hashCode();
        result = prime * result + schemaPath.hashCode();
        result = prime * result + position.hashCode();
        result = prime * result + ((unknownSchemaNodes == null) ? 0 : unknownSchemaNodes.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BitsTypeDefinition.Bit other = (BitsTypeDefinition.Bit) obj;
        if (qName == null) {
            if (other.getQName() != null) {
                return false;
            }
        } else if (!qName.equals(other.getQName())) {
            return false;
        }
        if (schemaPath == null) {
            if (other.getPath() != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.getPath())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return BitEffectiveStatementImpl.class.getSimpleName() + "[name=" + qName.getLocalName() + ", position="
                + position + "]";
    }
}