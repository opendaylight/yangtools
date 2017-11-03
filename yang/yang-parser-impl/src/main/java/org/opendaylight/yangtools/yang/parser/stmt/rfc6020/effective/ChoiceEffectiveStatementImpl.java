/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangValidationBundles;

public final class ChoiceEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<ChoiceStatement> implements
        ChoiceSchemaNode, DerivableSchemaNode {

    private final Set<AugmentationSchemaNode> augmentations;
    private final SortedMap<QName, ChoiceCaseNode> cases;
    private final ChoiceCaseNode defaultCase;
    private final ChoiceSchemaNode original;
    private final boolean mandatory;

    public ChoiceEffectiveStatementImpl(
            final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
        super(ctx);
        this.original = (ChoiceSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        // initSubstatementCollectionsAndFields
        final Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
        final SortedMap<QName, ChoiceCaseNode> casesInit = new TreeMap<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof AugmentationSchemaNode) {
                final AugmentationSchemaNode augmentationSchema = (AugmentationSchemaNode) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof ChoiceCaseNode) {
                final ChoiceCaseNode choiceCaseNode = (ChoiceCaseNode) effectiveStatement;
                // FIXME: we may be overwriting a previous entry, is that really okay?
                casesInit.put(choiceCaseNode.getQName(), choiceCaseNode);
            }
            if (YangValidationBundles.SUPPORTED_CASE_SHORTHANDS.contains(effectiveStatement.statementDefinition())) {
                final DataSchemaNode dataSchemaNode = (DataSchemaNode) effectiveStatement;
                final ChoiceCaseNode shorthandCase = new CaseShorthandImpl(dataSchemaNode);
                // FIXME: we may be overwriting a previous entry, is that really okay?
                casesInit.put(shorthandCase.getQName(), shorthandCase);
                if (dataSchemaNode.isAugmenting() && !this.augmenting) {
                    resetAugmenting(dataSchemaNode);
                }
            }
        }

        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.cases = ImmutableSortedMap.copyOfSorted(casesInit);

        final DefaultEffectiveStatementImpl defaultStmt = firstEffective(DefaultEffectiveStatementImpl.class);
        if (defaultStmt != null) {
            final QName qname;
            try {
                qname = QName.create(getQName(), defaultStmt.argument());
            } catch (IllegalArgumentException e) {
                throw new SourceException(ctx.getStatementSourceReference(), "Default statement has invalid name '%s'",
                    defaultStmt.argument(), e);
            }

            // FIXME: this does not work with submodules, as they are
            defaultCase = InferenceException.throwIfNull(cases.get(qname), ctx.getStatementSourceReference(),
                "Default statement refers to missing case %s", qname);
        } else {
            defaultCase = null;
        }

        mandatory = Boolean.TRUE.equals(StmtContextUtils.firstSubstatementAttributeOf(ctx, MandatoryStatement.class));
    }

    private static void resetAugmenting(final DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof LeafEffectiveStatementImpl) {
            final LeafEffectiveStatementImpl leaf = (LeafEffectiveStatementImpl) dataSchemaNode;
            leaf.augmenting = false;
        } else if (dataSchemaNode instanceof ContainerEffectiveStatementImpl) {
            final ContainerEffectiveStatementImpl container = (ContainerEffectiveStatementImpl) dataSchemaNode;
            container.augmenting = false;
        } else if (dataSchemaNode instanceof LeafListEffectiveStatementImpl) {
            final LeafListEffectiveStatementImpl leafList = (LeafListEffectiveStatementImpl) dataSchemaNode;
            leafList.augmenting = false;
        } else if (dataSchemaNode instanceof ListEffectiveStatementImpl) {
            final ListEffectiveStatementImpl list = (ListEffectiveStatementImpl) dataSchemaNode;
            list.augmenting = false;
        } else if (dataSchemaNode instanceof AnyXmlEffectiveStatementImpl) {
            final AnyXmlEffectiveStatementImpl anyXml = (AnyXmlEffectiveStatementImpl) dataSchemaNode;
            anyXml.augmenting = false;
        }
    }

    @Override
    public Optional<ChoiceSchemaNode> getOriginal() {
        return Optional.ofNullable(original);
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return augmentations;
    }

    @Override
    public SortedMap<QName, ChoiceCaseNode> getCases() {
        return cases;
    }

    @Override
    public Optional<ChoiceCaseNode> getDefaultCase() {
        return Optional.ofNullable(defaultCase);
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
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
        final ChoiceEffectiveStatementImpl other = (ChoiceEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return ChoiceEffectiveStatementImpl.class.getSimpleName() + "["
                + "qname=" + getQName()
                + "]";
    }
}
