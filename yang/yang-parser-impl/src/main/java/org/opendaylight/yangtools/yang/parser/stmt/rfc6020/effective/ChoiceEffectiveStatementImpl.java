/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class ChoiceEffectiveStatementImpl extends
        AbstractEffectiveDocumentedNode<QName, ChoiceStatement> implements
        ChoiceSchemaNode, DerivableSchemaNode {
    private final QName qname;
    private final SchemaPath path;

    boolean augmenting;
    boolean addedByUses;
    ChoiceSchemaNode original;
    boolean configuration = true;
    ConstraintDefinition constraints;
    String defaultCase;

    ImmutableSet<ChoiceCaseNode> cases;
    ImmutableSet<AugmentationSchema> augmentations;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    public ChoiceEffectiveStatementImpl(
            StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
        super(ctx);

        this.qname = ctx.getStatementArgument();
        this.path = Utils.getSchemaPath(ctx);
        this.constraints = new EffectiveConstraintDefinitionImpl(this);

        initCopyType(ctx);
        initSubstatementCollectionsAndFields();
    }

    private void initCopyType(
            StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {

        List<TypeOfCopy> copyTypesFromOriginal = ctx.getCopyHistory();

        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_AUGMENTATION)) {
            augmenting = true;
        }
        if (copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
        if (copyTypesFromOriginal
                .contains(TypeOfCopy.ADDED_BY_USES_AUGMENTATION)) {
            addedByUses = augmenting = true;
        }

        if (ctx.getOriginalCtx() != null) {
            original = (ChoiceSchemaNode) ctx.getOriginalCtx().buildEffective();
        }
    }

    private void initSubstatementCollectionsAndFields() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        List<UnknownSchemaNode> unknownNodesInit = new LinkedList<>();
        Set<AugmentationSchema> augmentationsInit = new HashSet<>();
        Set<ChoiceCaseNode> casesInit = new HashSet<>();

        boolean configurationInit = false;
        boolean defaultInit = false;
        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodesInit.add(unknownNode);
            }
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof ChoiceCaseNode) {
                ChoiceCaseNode choiceCaseNode = (ChoiceCaseNode) effectiveStatement;
                casesInit.add(choiceCaseNode);
            }
            if (effectiveStatement instanceof AnyXmlSchemaNode
                    || effectiveStatement instanceof ContainerSchemaNode
                    || effectiveStatement instanceof ListSchemaNode
                    || effectiveStatement instanceof LeafListSchemaNode
                    || effectiveStatement instanceof LeafSchemaNode) {

                DataSchemaNode dataSchemaNode = (DataSchemaNode) effectiveStatement;
                ChoiceCaseNode shorthandCase = new CaseShorthandImpl(
                        dataSchemaNode);
                casesInit.add(shorthandCase);

                if (dataSchemaNode.isAugmenting() == true
                        && this.augmenting == false) {
                    resetAugmenting(dataSchemaNode);
                }
            }
            if (!configurationInit
                    && effectiveStatement instanceof ConfigEffectiveStatementImpl) {
                ConfigEffectiveStatementImpl configStmt = (ConfigEffectiveStatementImpl) effectiveStatement;
                this.configuration = configStmt.argument();
                configurationInit = true;
            }
            if (!defaultInit
                    && effectiveStatement instanceof DefaultEffectiveStatementImpl) {
                DefaultEffectiveStatementImpl defaultCaseStmt = (DefaultEffectiveStatementImpl) effectiveStatement;
                this.defaultCase = defaultCaseStmt.argument();
                defaultInit = true;
            }
        }

        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);
        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.cases = ImmutableSet.copyOf(casesInit);
    }

    private void resetAugmenting(DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof LeafEffectiveStatementImpl) {
            LeafEffectiveStatementImpl leaf = (LeafEffectiveStatementImpl) dataSchemaNode;
            leaf.augmenting = false;
        } else if (dataSchemaNode instanceof ContainerEffectiveStatementImpl) {
            ContainerEffectiveStatementImpl container = (ContainerEffectiveStatementImpl) dataSchemaNode;
            container.augmenting = false;
        } else if (dataSchemaNode instanceof LeafListEffectiveStatementImpl) {
            LeafListEffectiveStatementImpl leafList = (LeafListEffectiveStatementImpl) dataSchemaNode;
            leafList.augmenting = false;
        } else if (dataSchemaNode instanceof ListEffectiveStatementImpl) {
            ListEffectiveStatementImpl list = (ListEffectiveStatementImpl) dataSchemaNode;
            list.augmenting = false;
        } else if (dataSchemaNode instanceof AnyXmlEffectiveStatementImpl) {
            AnyXmlEffectiveStatementImpl anyXml = (AnyXmlEffectiveStatementImpl) dataSchemaNode;
            anyXml.augmenting = false;
        }
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
    public Optional<ChoiceSchemaNode> getOriginal() {
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public Set<ChoiceCaseNode> getCases() {
        return cases;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final QName name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Choice Case QName cannot be NULL!");
        }
        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && name.equals(caseNode.getQName())) {
                return caseNode;
            }
        }
        return null;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final String name) {
        if (name == null) {
            throw new IllegalArgumentException(
                    "Choice Case string Name cannot be NULL!");
        }
        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && (caseNode.getQName() != null)
                    && name.equals(caseNode.getQName().getLocalName())) {
                return caseNode;
            }
        }
        return null;
    }

    @Override
    public String getDefaultCase() {
        return defaultCase;
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
        ChoiceEffectiveStatementImpl other = (ChoiceEffectiveStatementImpl) obj;
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
        StringBuilder sb = new StringBuilder(
                ChoiceEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=").append(qname);
        sb.append("]");
        return sb.toString();
    }

}