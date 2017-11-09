/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.deviate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.util.OptionalBoolean;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class DeviateEffectiveStatementImpl extends DeclaredEffectiveStatementBase<DeviateKind, DeviateStatement>
        implements DeviateDefinition, DeviateEffectiveStatement {

    private final DeviateKind deviateType;
    private final String deviatedDefault;
    private final Integer deviatedMaxElements;
    private final Integer deviatedMinElements;
    private final Set<MustDefinition> deviatedMustDefinitions;
    private final TypeDefinition<?> deviatedType;
    private final Collection<UniqueConstraint> deviatedUniqueConstraints;
    private final String deviatedUnits;

    private final byte deviatedConfig;
    private final byte deviatedMandatory;

    DeviateEffectiveStatementImpl(final StmtContext<DeviateKind, DeviateStatement, ?> ctx) {
        super(ctx);

        this.deviateType = argument();

        final ConfigEffectiveStatement configStmt = firstEffective(ConfigEffectiveStatement.class);
        this.deviatedConfig = OptionalBoolean.ofNullable(configStmt == null ? null : configStmt.argument());
        final DefaultEffectiveStatement defaultStmt = firstEffective(DefaultEffectiveStatement.class);
        this.deviatedDefault = defaultStmt == null ? null : defaultStmt.argument();
        final MandatoryEffectiveStatement mandatoryStmt = firstEffective(MandatoryEffectiveStatement.class);
        this.deviatedMandatory = OptionalBoolean.ofNullable(mandatoryStmt == null ? null : mandatoryStmt.argument());
        final MaxElementsEffectiveStatement maxElementsStmt = firstEffective(MaxElementsEffectiveStatement.class);
        this.deviatedMaxElements = maxElementsStmt == null ? null : Integer.valueOf(maxElementsStmt.argument());
        final MinElementsEffectiveStatement minElementsStmt = firstEffective(MinElementsEffectiveStatement.class);
        this.deviatedMinElements = minElementsStmt == null ? null : minElementsStmt.argument();
        final TypeEffectiveStatement<TypeStatement> typeStmt = firstEffective(TypeEffectiveStatement.class);
        this.deviatedType = typeStmt == null ? null : typeStmt.getTypeDefinition();
        final UnitsEffectiveStatement unitsStmt = firstEffective(UnitsEffectiveStatement.class);
        this.deviatedUnits = unitsStmt == null ? null : unitsStmt.argument();

        this.deviatedMustDefinitions = ImmutableSet.copyOf(allSubstatementsOfType(MustDefinition.class));
        this.deviatedUniqueConstraints = ImmutableList.copyOf(allSubstatementsOfType(UniqueConstraint.class));
    }

    @Override
    public DeviateKind getDeviateType() {
        return deviateType;
    }

    @Override
    public Boolean getDeviatedConfig() {
        return OptionalBoolean.toNullable(deviatedConfig);
    }

    @Override
    public String getDeviatedDefault() {
        return deviatedDefault;
    }

    @Override
    public Boolean getDeviatedMandatory() {
        return OptionalBoolean.toNullable(deviatedMandatory);
    }

    @Override
    public Integer getDeviatedMaxElements() {
        return deviatedMaxElements;
    }

    @Override
    public Integer getDeviatedMinElements() {
        return deviatedMinElements;
    }

    @Override
    public Set<MustDefinition> getDeviatedMusts() {
        return deviatedMustDefinitions;
    }

    @Override
    public TypeDefinition<?> getDeviatedType() {
        return deviatedType;
    }

    @Override
    public Collection<UniqueConstraint> getDeviatedUniques() {
        return deviatedUniqueConstraints;
    }

    @Override
    public String getDeviatedUnits() {
        return deviatedUnits;
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
        DeviateEffectiveStatementImpl other = (DeviateEffectiveStatementImpl) obj;
        return Objects.equals(deviateType, other.deviateType)
                && deviatedConfig == other.deviatedConfig
                && Objects.equals(deviatedDefault, other.deviatedDefault)
                && deviatedMandatory == other.deviatedMandatory
                && Objects.equals(deviatedMaxElements, other.deviatedMaxElements)
                && Objects.equals(deviatedMinElements, other.deviatedMinElements)
                && Objects.equals(deviatedMustDefinitions, other.deviatedMustDefinitions)
                && Objects.equals(deviatedType, other.deviatedType)
                && Objects.equals(deviatedUniqueConstraints, other.deviatedUniqueConstraints)
                && Objects.equals(deviatedUnits, other.deviatedUnits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviateType, deviatedConfig, deviatedDefault, deviatedMandatory, deviatedMaxElements,
                deviatedMinElements, deviatedMustDefinitions, deviatedType, deviatedUniqueConstraints, deviatedUnits);
    }
}
