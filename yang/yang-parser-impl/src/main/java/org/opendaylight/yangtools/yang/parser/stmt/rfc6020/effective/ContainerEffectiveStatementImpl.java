/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import com.google.common.base.Optional;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class ContainerEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, ContainerStatement>
        implements ContainerSchemaNode, DerivableSchemaNode {

    private final QName qname;
    private final SchemaPath path;

    private boolean presence;
    private boolean augmenting;
    private boolean addedByUses;
    private boolean configuration = true;
    private ContainerSchemaNode original;
    private ConstraintDefinition constraints;

    private ImmutableSet<AugmentationSchema> augmentations;
    private ImmutableList<UnknownSchemaNode> unknownNodes;

    public ContainerEffectiveStatementImpl(
            StmtContext<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument();
        path = Utils.getSchemaPath(ctx);
        this.constraints = new EffectiveConstraintDefinitionImpl(this);

        initCopyType(ctx);
        initSubstatementCollectionsAndFields();
    }

    private void initCopyType(
            StmtContext<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> ctx) {

        Set<TypeOfCopy> copyTypesFromOriginal = StmtContextUtils.getCopyTypesFromOriginal(ctx);

        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION)) {
            augmenting = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            addedByUses = augmenting = true;
        }

        if (ctx.getTypeOfCopy() != TypeOfCopy.ORIGINAL) {
            original = (ContainerSchemaNode) ctx.getOriginalCtx().buildEffective();
        }
    }

    private void initSubstatementCollectionsAndFields() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();

        boolean configurationInit = false;
        for (EffectiveStatement<?, ?> effectiveSubstatement : effectiveSubstatements) {
            if (effectiveSubstatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveSubstatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveSubstatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveSubstatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveSubstatement instanceof PresenceEffectiveStatementImpl) {
                presence = true;
            }
            if (!configurationInit
                    && effectiveSubstatement instanceof ConfigEffectiveStatementImpl) {
                ConfigEffectiveStatementImpl configStmt = (ConfigEffectiveStatementImpl) effectiveSubstatement;
                this.configuration = configStmt.argument();
                configurationInit = true;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
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
    public Optional<ContainerSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public boolean isConfiguration() {
        return configuration;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        return constraints;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public boolean isPresenceContainer() {
        return presence;
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
        ContainerEffectiveStatementImpl other = (ContainerEffectiveStatementImpl) obj;
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
        return "container " + qname.getLocalName();
    }
}