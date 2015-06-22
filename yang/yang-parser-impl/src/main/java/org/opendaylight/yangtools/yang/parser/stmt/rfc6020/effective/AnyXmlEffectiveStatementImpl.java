/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import java.util.Collection;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class AnyXmlEffectiveStatementImpl extends
        AbstractEffectiveDocumentedNode<QName, AnyxmlStatement> implements
        AnyXmlSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    boolean configuration = true;
    AnyXmlSchemaNode original;
    ConstraintDefinition constraintsDef;
    boolean augmenting;
    boolean addedByUses;

    ImmutableList<UnknownSchemaNode> unknownNodes;

    public AnyXmlEffectiveStatementImpl(
            StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> ctx) {
        super(ctx);
        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        this.constraintsDef = new EffectiveConstraintDefinitionImpl(this);

        initSubstatementCollectionsAndFields();
        initCopyType(ctx);
    }

    private void initCopyType(
            StmtContext<QName, AnyxmlStatement, EffectiveStatement<QName, AnyxmlStatement>> ctx) {

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
            original = (AnyXmlSchemaNode) ctx.getOriginalCtx().buildEffective();
        }
    }

    private void initSubstatementCollectionsAndFields() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();

        boolean configurationInit = false;
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (!configurationInit
                    && effectiveStatement instanceof ConfigEffectiveStatementImpl) {
                ConfigEffectiveStatementImpl configStmt = (ConfigEffectiveStatementImpl) effectiveStatement;
                this.configuration = configStmt.argument();
                configurationInit = true;
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
    public Optional<AnyXmlSchemaNode> getOriginal() {
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    private boolean checkQname(AnyXmlEffectiveStatementImpl other) {
      if (qname == null) {
        if (other.qname != null) {
          return false;
        }
      } else if (!qname.equals(other.qname)) {
        return false;
      }
      return true;
    }

    private boolean checkPath(AnyXmlEffectiveStatementImpl other) {
      if (path == null) {
        if (other.path != null) {
            return false;
        }
      } else if (!path.equals(other.path)) {
        return false;
      }
      return true;
    }

    private boolean checkObject(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!checkObject(obj)) {
          return false;
        }

        AnyXmlEffectiveStatementImpl other = (AnyXmlEffectiveStatementImpl) obj;

        if (!checkQname(other)) {
            return false;
        }

        if (!checkPath(other)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(
                AnyXmlEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=").append(qname);
        sb.append(", path=").append(path);
        sb.append("]");
        return sb.toString();
    }

}
