/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

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
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDataSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class ChoiceEffectiveStatementImpl extends AbstractEffectiveDataSchemaNode<ChoiceStatement>
        implements ChoiceEffectiveStatement, ChoiceSchemaNode, DerivableSchemaNode {

    private final ImmutableSet<AugmentationSchemaNode> augmentations;
    private final ImmutableSortedMap<QName, CaseSchemaNode> cases;
    private final CaseSchemaNode defaultCase;
    private final ChoiceSchemaNode original;
    private final boolean mandatory;

    ChoiceEffectiveStatementImpl(
            final StmtContext<QName, ChoiceStatement, EffectiveStatement<QName, ChoiceStatement>> ctx) {
        super(ctx);
        this.original = (ChoiceSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective).orElse(null);

        // initSubstatementCollectionsAndFields
        final Set<AugmentationSchemaNode> augmentationsInit = new LinkedHashSet<>();
        final SortedMap<QName, CaseSchemaNode> casesInit = new TreeMap<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof AugmentationSchemaNode) {
                final AugmentationSchemaNode augmentationSchema = (AugmentationSchemaNode) effectiveStatement;
                augmentationsInit.add(augmentationSchema);
            }
            if (effectiveStatement instanceof CaseSchemaNode) {
                final CaseSchemaNode choiceCaseNode = (CaseSchemaNode) effectiveStatement;
                // FIXME: we may be overwriting a previous entry, is that really okay?
                casesInit.put(choiceCaseNode.getQName(), choiceCaseNode);
            }
        }

        this.augmentations = ImmutableSet.copyOf(augmentationsInit);
        this.cases = ImmutableSortedMap.copyOfSorted(casesInit);

        final Optional<String> defaultArg = findFirstEffectiveSubstatementArgument(DefaultEffectiveStatement.class);
        if (defaultArg.isPresent()) {
            final String arg = defaultArg.get();
            final QName qname;
            try {
                qname = QName.create(getQName(), arg);
            } catch (IllegalArgumentException e) {
                throw new SourceException(ctx.getStatementSourceReference(), "Default statement has invalid name '%s'",
                    arg, e);
            }

            // FIXME: this does not work with submodules, as they are
            defaultCase = InferenceException.throwIfNull(cases.get(qname), ctx.getStatementSourceReference(),
                "Default statement refers to missing case %s", qname);
        } else {
            defaultCase = null;
        }

        mandatory = findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(Boolean.FALSE)
                .booleanValue();
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
    public SortedMap<QName, CaseSchemaNode> getCases() {
        return cases;
    }

    @Override
    public Optional<CaseSchemaNode> getDefaultCase() {
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
