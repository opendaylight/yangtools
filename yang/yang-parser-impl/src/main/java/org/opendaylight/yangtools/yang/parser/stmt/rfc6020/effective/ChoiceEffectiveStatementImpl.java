/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class ChoiceEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<ChoiceStatement> implements
        ChoiceSchemaNode, DerivableSchemaNode {
    /**
     * Comparator based on alphabetical order of local name of SchemaNode's
     * qname.
     */
    private static final Comparator<SchemaNode> SCHEMA_NODE_COMP = (o1, o2) -> {
        return o1.getQName().compareTo(o2.getQName());
    };

    private final ChoiceSchemaNode original;
    private final String defaultCase;

    private final Set<ChoiceCaseNode> cases;
    private final Set<AugmentationSchema> augmentations;

    public ChoiceEffectiveStatementImpl(
            final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
        super(ctx);
        this.original = ctx.getOriginalCtx() == null ? null : (ChoiceSchemaNode) ctx.getOriginalCtx().buildEffective();

        DefaultEffectiveStatementImpl defaultStmt = firstEffective(DefaultEffectiveStatementImpl.class);
        this.defaultCase = (defaultStmt == null) ? null : defaultStmt.argument();

        // initSubstatementCollectionsAndFields
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();
        Set<AugmentationSchema> augmentationsInit = new LinkedHashSet<>();
        SortedSet<ChoiceCaseNode> casesInit = new TreeSet<>(SCHEMA_NODE_COMP);

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof AugmentationSchema) {
                AugmentationSchema augmentationSchema = (AugmentationSchema) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof ChoiceCaseNode) {
                ChoiceCaseNode choiceCaseNode = (ChoiceCaseNode) effectiveStatement;
                casesInit.add(choiceCaseNode);
            }
            if (effectiveStatement instanceof AnyXmlSchemaNode || effectiveStatement instanceof ContainerSchemaNode
                    || effectiveStatement instanceof ListSchemaNode || effectiveStatement instanceof LeafListSchemaNode
                    || effectiveStatement instanceof LeafSchemaNode) {

                DataSchemaNode dataSchemaNode = (DataSchemaNode) effectiveStatement;
                ChoiceCaseNode shorthandCase = new CaseShorthandImpl(dataSchemaNode);
                casesInit.add(shorthandCase);

                if (dataSchemaNode.isAugmenting() && !this.augmenting) {
                    resetAugmenting(dataSchemaNode);
                }
            }
        }

        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.cases = ImmutableSet.copyOf(casesInit);
    }

    private static void resetAugmenting(final DataSchemaNode dataSchemaNode) {
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
    public Optional<ChoiceSchemaNode> getOriginal() {
        return Optional.fromNullable(original);
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public Set<ChoiceCaseNode> getCases() {
        return cases;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final QName name) {
        Preconditions.checkArgument(name != null, "Choice Case QName cannot be NULL!");

        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && name.equals(caseNode.getQName())) {
                return caseNode;
            }
        }
        return null;
    }

    @Override
    public ChoiceCaseNode getCaseNodeByName(final String name) {
        Preconditions.checkArgument(name != null, "Choice Case string Name cannot be NULL!");

        for (final ChoiceCaseNode caseNode : cases) {
            if (caseNode != null && (caseNode.getQName() != null) && name.equals(caseNode.getQName().getLocalName())) {
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
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
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
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return ChoiceEffectiveStatementImpl.class.getSimpleName() + "[" +
                "qname=" + getQName() +
                "]";
    }
}
