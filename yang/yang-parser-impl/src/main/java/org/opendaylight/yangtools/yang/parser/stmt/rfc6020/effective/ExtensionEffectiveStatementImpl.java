/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ExtensionEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, ExtensionStatement>
        implements ExtensionDefinition {
    private final QName qname;
    private final String argument;
    private final SchemaPath schemaPath;

    private final List<UnknownSchemaNode> unknownNodes;
    private final boolean yin;

    public ExtensionEffectiveStatementImpl(
            final StmtContext<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.schemaPath = ctx.getSchemaPath().get();

        // initSubstatementCollections
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();
        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);

        // initFields
        ArgumentEffectiveStatementImpl argumentSubstatement = firstEffective(ArgumentEffectiveStatementImpl.class);
        if (argumentSubstatement != null) {
            this.argument = argumentSubstatement.argument().getLocalName();

            YinElementEffectiveStatementImpl yinElement = argumentSubstatement
                    .firstEffective(YinElementEffectiveStatementImpl.class);
            if (yinElement != null) {
                this.yin = yinElement.argument();
            } else {
                this.yin = false;
            }
        } else {
            this.argument = null;
            this.yin = false;
        }
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    @Override
    public boolean isYinElement() {
        return yin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(schemaPath);
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
        ExtensionEffectiveStatementImpl other = (ExtensionEffectiveStatementImpl) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(schemaPath, other.schemaPath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ExtensionEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("argument=").append(argument);
        sb.append(", qname=").append(qname);
        sb.append(", schemaPath=").append(schemaPath);
        sb.append(", extensionSchemaNodes=").append(unknownNodes);
        sb.append(", yin=").append(yin);
        sb.append("]");
        return sb.toString();
    }
}
