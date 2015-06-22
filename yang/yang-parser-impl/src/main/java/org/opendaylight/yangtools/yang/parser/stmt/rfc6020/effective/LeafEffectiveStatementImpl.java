/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class LeafEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, LeafStatement> implements
        LeafSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    private boolean augmenting;
    private boolean addedByUses;
    private LeafSchemaNode original;
    private boolean configuration = true;
    private ConstraintDefinition constraintsDef;
    private TypeDefinition<?> type;
    private String defaultStr;
    private String unitsStr;

    private ImmutableList<UnknownSchemaNode> unknownNodes;

    public LeafEffectiveStatementImpl(StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        this.constraintsDef = new EffectiveConstraintDefinitionImpl(this);

        initSubstatementCollections(ctx);
        initCopyType(ctx);
    }

    private void initCopyType(
            StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {

        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();

        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION)) {
            augmenting = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            addedByUses = augmenting = true;
        }

        if (ctx.getOriginalCtx() != null) {
            original = (LeafSchemaNode) ctx.getOriginalCtx().buildEffective();
        }
    }

    private void initSubstatementCollections(
            final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        boolean configurationInit = false;
        boolean defaultInit = false;
        boolean unitsInit = false;
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                unknownNodesInit.add((UnknownSchemaNode) effectiveStatement);
            }
            if (effectiveStatement instanceof TypeDefinition) {
                type = TypeUtils.getTypeFromEffectiveStatement(effectiveStatement);
            }
            if (!configurationInit && effectiveStatement instanceof ConfigEffectiveStatementImpl) {
                ConfigEffectiveStatementImpl configStmt = (ConfigEffectiveStatementImpl) effectiveStatement;
                this.configuration = configStmt.argument();
                configurationInit = true;
            }
            if (!defaultInit && effectiveStatement instanceof DefaultEffectiveStatementImpl) {
                DefaultEffectiveStatementImpl defStmt = (DefaultEffectiveStatementImpl) effectiveStatement;
                this.defaultStr = defStmt.argument();
                defaultInit = true;
            }
            if (!unitsInit && effectiveStatement instanceof UnitsEffectiveStatementImpl) {
                UnitsEffectiveStatementImpl unitStmt = (UnitsEffectiveStatementImpl) effectiveStatement;
                this.unitsStr = unitStmt.argument();
                unitsInit = true;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public Optional<LeafSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraintsDef;
    }

    @Override
    public TypeDefinition<?> getType() {
        return type;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String getDefault() {
        return defaultStr;
    }

    @Override
    public String getUnits() {
        return unitsStr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        LeafEffectiveStatementImpl other = (LeafEffectiveStatementImpl) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(LeafEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=").append(qname);
        sb.append(", path=").append(path);
        sb.append("]");
        return sb.toString();
    }
}