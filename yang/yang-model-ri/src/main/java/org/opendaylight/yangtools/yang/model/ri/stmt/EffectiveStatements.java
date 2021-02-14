/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyStatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyYangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyBelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyDescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyFractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyIfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyIncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyMinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyNamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyOrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyOrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyPresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyRevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyStatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyUnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyWhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyYangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.EmptyYinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularBaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularBelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularDescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularFractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularIfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularIncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularMinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularNamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularOrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularOrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularPresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularRevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularStatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularUnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularWhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularYangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff.RegularYinElementEffectiveStatement;

/**
 * Static entry point to instantiating {@link EffectiveStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class EffectiveStatements {
    private EffectiveStatements() {
        // Hidden on purpose
    }

    public static ArgumentEffectiveStatement createArgument(final ArgumentStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyArgumentEffectiveStatement(declared)
            : new RegularArgumentEffectiveStatement(declared, substatements);
    }

    public static BaseEffectiveStatement createBase(final BaseStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBaseEffectiveStatement(declared)
            : new RegularBaseEffectiveStatement(declared, substatements);
    }

    public static BelongsToEffectiveStatement createBelongsTo(final BelongsToStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyBelongsToEffectiveStatement(declared)
            : new RegularBelongsToEffectiveStatement(declared, substatements);
    }

    public static DescriptionEffectiveStatement createDescription(final DescriptionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyDescriptionEffectiveStatement(declared)
            : new RegularDescriptionEffectiveStatement(declared, substatements);
    }

    public static ErrorAppTagEffectiveStatement createErrorAppTag(final ErrorAppTagStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorAppTagEffectiveStatement(declared)
            : new RegularErrorAppTagEffectiveStatement(declared, substatements);
    }

    public static ErrorMessageEffectiveStatement createErrorMessage(final ErrorMessageStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorMessageEffectiveStatement(declared)
            : new RegularErrorMessageEffectiveStatement(declared, substatements);
    }

    public static FractionDigitsEffectiveStatement createFractionDigits(final FractionDigitsStatement declared) {
        return new EmptyFractionDigitsEffectiveStatement(declared);
    }

    public static FractionDigitsEffectiveStatement createFractionDigits(final FractionDigitsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createFractionDigits(declared)
            : new RegularFractionDigitsEffectiveStatement(declared, substatements);
    }

    public static IfFeatureEffectiveStatement createIfFeature(final IfFeatureStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyIfFeatureEffectiveStatement(declared)
            : new RegularIfFeatureEffectiveStatement(declared, substatements);
    }

    public static IncludeEffectiveStatement createInclude(final IncludeStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyIncludeEffectiveStatement(declared)
            : new RegularIncludeEffectiveStatement(declared, substatements);
    }

    public static MandatoryEffectiveStatement createMandatory(final MandatoryStatement declared) {
        return new EmptyMandatoryEffectiveStatement(declared);
    }

    public static MandatoryEffectiveStatement createMandatory(final MandatoryStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createMandatory(declared)
            : new RegularMandatoryEffectiveStatement(declared, substatements);
    }

    public static MaxElementsEffectiveStatement createMaxElements(final MaxElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyMaxElementsEffectiveStatement(declared)
            : new RegularMaxElementsEffectiveStatement(declared, substatements);
    }

    public static MinElementsEffectiveStatement createMinElements(final MinElementsStatement declared) {
        return new EmptyMinElementsEffectiveStatement(declared);
    }

    public static MinElementsEffectiveStatement createMinElements(final MinElementsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createMinElements(declared)
            : new RegularMinElementsEffectiveStatement(declared, substatements);
    }

    public static ModifierEffectiveStatement createModifier(final ModifierStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyModifierEffectiveStatement(declared)
            : new RegularModifierEffectiveStatement(declared, substatements);
    }

    public static NamespaceEffectiveStatement createNamespace(final NamespaceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyNamespaceEffectiveStatement(declared)
            : new RegularNamespaceEffectiveStatement(declared, substatements);
    }

    public static OrderedByEffectiveStatement createOrderedBy(final OrderedByStatement declared) {
        return new EmptyOrderedByEffectiveStatement(declared);
    }

    public static OrderedByEffectiveStatement createOrderedBy(final OrderedByStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createOrderedBy(declared)
            : new RegularOrderedByEffectiveStatement(declared, substatements);
    }

    public static OrganizationEffectiveStatement createOrganization(final OrganizationStatement declared) {
        return new EmptyOrganizationEffectiveStatement(declared);
    }

    public static OrganizationEffectiveStatement createOrganization(final OrganizationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createOrganization(declared)
            : new RegularOrganizationEffectiveStatement(declared, substatements);
    }

    public static PathEffectiveStatement createPath(final PathStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPathEffectiveStatement(declared)
            : new RegularPathEffectiveStatement(declared, substatements);
    }

    public static PositionEffectiveStatement createPosition(final PositionStatement declared) {
        return new EmptyPositionEffectiveStatement(declared);
    }

    public static PositionEffectiveStatement createPosition(final PositionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createPosition(declared)
            : new RegularPositionEffectiveStatement(declared, substatements);
    }

    public static ReferenceEffectiveStatement createReference(final ReferenceStatement declared) {
        return new EmptyReferenceEffectiveStatement(declared);
    }

    public static ReferenceEffectiveStatement createReference(final ReferenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createReference(declared)
            : new RegularReferenceEffectiveStatement(declared, substatements);
    }

    public static PresenceEffectiveStatement createPresence(final PresenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyPresenceEffectiveStatement(declared)
            : new RegularPresenceEffectiveStatement(declared, substatements);
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final boolean argument) {
        return argument ? EmptyRequireInstanceEffectiveStatement.TRUE : EmptyRequireInstanceEffectiveStatement.FALSE;
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final RequireInstanceStatement declared) {
        if (EmptyRequireInstanceStatement.TRUE.equals(declared)) {
            return EmptyRequireInstanceEffectiveStatement.TRUE;
        } else if (EmptyRequireInstanceStatement.FALSE.equals(declared)) {
            return EmptyRequireInstanceEffectiveStatement.FALSE;
        } else {
            return new EmptyRequireInstanceEffectiveStatement(declared);
        }
    }

    public static RequireInstanceEffectiveStatement createRequireInstance(final RequireInstanceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createRequireInstance(declared)
            : new RegularRequireInstanceEffectiveStatement(declared, substatements);
    }

    public static RevisionDateEffectiveStatement createRevisionDate(final RevisionDateStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyRevisionDateEffectiveStatement(declared)
            : new RegularRevisionDateEffectiveStatement(declared, substatements);
    }

    public static StatusEffectiveStatement createStatus(final StatusStatement declared) {
        // Aggressively reuse effective instances which are backed by the corresponding empty declared instance, as this
        // is the case unless there is a weird extension in use.
        if (EmptyStatusStatement.DEPRECATED.equals(declared)) {
            // Most likely to be seen (as current is the default)
            return EmptyStatusEffectiveStatement.DEPRECATED;
        } else if (EmptyStatusStatement.OBSOLETE.equals(declared)) {
            // less likely
            return EmptyStatusEffectiveStatement.OBSOLETE;
        } else if (EmptyStatusStatement.CURRENT.equals(declared)) {
            // ... okay, why is this there? :)
            return EmptyStatusEffectiveStatement.CURRENT;
        } else {
            return new EmptyStatusEffectiveStatement(declared);
        }
    }

    public static StatusEffectiveStatement createStatus(final StatusStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createStatus(declared)
            : new RegularStatusEffectiveStatement(declared, substatements);
    }

    public static UnitsEffectiveStatement createUnits(final UnitsStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyUnitsEffectiveStatement(declared)
            : new RegularUnitsEffectiveStatement(declared, substatements);
    }

    public static ValueEffectiveStatement createValue(final ValueStatement declared) {
        return new EmptyValueEffectiveStatement(declared);
    }

    public static ValueEffectiveStatement createValue(final ValueStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createValue(declared)
            : new RegularValueEffectiveStatement(declared, substatements);
    }

    public static WhenEffectiveStatement createWhen(final WhenStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? new EmptyWhenEffectiveStatement(declared)
            : new RegularWhenEffectiveStatement(declared, substatements);
    }

    public static YangVersionEffectiveStatement createYangVersion(final YangVersionStatement declared) {
        if (EmptyYangVersionStatement.VERSION_1.equals(declared)) {
            return EmptyYangVersionEffectiveStatement.VERSION_1;
        } else if (EmptyYangVersionStatement.VERSION_1_1.equals(declared)) {
            return EmptyYangVersionEffectiveStatement.VERSION_1_1;
        } else {
            return new EmptyYangVersionEffectiveStatement(declared);
        }
    }

    public static YangVersionEffectiveStatement createYangVersion(final YangVersionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createYangVersion(declared)
            : new RegularYangVersionEffectiveStatement(declared, substatements);
    }

    public static YinElementEffectiveStatement createYinElement(final YinElementStatement declared) {
        return new EmptyYinElementEffectiveStatement(declared);
    }

    public static YinElementEffectiveStatement createYinElement(final YinElementStatement declared,
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.isEmpty() ? createYinElement(declared)
            : new RegularYinElementEffectiveStatement(declared, substatements);
    }
}
