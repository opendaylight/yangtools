/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument.WithSubstatements;

final class DeviateEffectiveStatementImpl extends WithSubstatements<DeviateKind, DeviateStatement>
        implements DeviateDefinition, DeviateEffectiveStatement {
    DeviateEffectiveStatementImpl(final DeviateStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared, substatements);
    }

    @Override
    public DeviateKind getDeviateType() {
        return argument();
    }

    @Override
    public Boolean getDeviatedConfig() {
        return findFirstEffectiveSubstatementArgument(ConfigEffectiveStatement.class).orElse(null);
    }

    @Override
    public String getDeviatedDefault() {
        return findFirstEffectiveSubstatementArgument(DefaultEffectiveStatement.class).orElse(null);
    }

    @Override
    public Boolean getDeviatedMandatory() {
        return findFirstEffectiveSubstatementArgument(MandatoryEffectiveStatement.class).orElse(null);
    }

    @Override
    public Integer getDeviatedMaxElements() {
        return findFirstEffectiveSubstatementArgument(MaxElementsEffectiveStatement.class)
                // FIXME: this does not handle 'unbounded'
                .map(Integer::valueOf).orElse(null);
    }

    @Override
    public Integer getDeviatedMinElements() {
        return findFirstEffectiveSubstatementArgument(MinElementsEffectiveStatement.class).orElse(null);
    }

    @Override
    public Collection<? extends MustDefinition> getDeviatedMusts() {
        return allSubstatementsOfType(MustDefinition.class);
    }

    @Override
    public TypeDefinition<?> getDeviatedType() {
        return findFirstEffectiveSubstatement(TypeEffectiveStatement.class)
                .map(TypeEffectiveStatement::getTypeDefinition).orElse(null);
    }

    @Override
    public Collection<? extends UniqueEffectiveStatement> getDeviatedUniques() {
        return allSubstatementsOfType(UniqueEffectiveStatement.class);
    }

    @Override
    public String getDeviatedUnits() {
        return findFirstEffectiveSubstatementArgument(UnitsEffectiveStatement.class).orElse(null);
    }

    @Override
    public DeviateEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
    }
}
