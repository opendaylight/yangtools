/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.DeviateStatementImpl;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.DeviationStatementImpl;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyActionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyAnydataStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyAnyxmlStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyArgumentStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyAugmentStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyBaseStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyBelongsToStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyBitStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyChoiceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyConfigStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyContactStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyContainerStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyDefaultStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyDescriptionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyEnumStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyExtensionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyFeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyFractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyGroupingStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyIdentityStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyIfFeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyIncludeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyInputStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyKeyStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyLeafListStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyLeafStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyLengthStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyListStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyMandatoryStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyMaxElementsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyMinElementsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyModifierStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyMustStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyNamespaceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyNotificationStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyOrderedByStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyOrganizationStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyOutputStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyPathStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyPatternStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyPositionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyPrefixStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyPresenceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyRangeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyReferenceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyRevisionDateStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyRevisionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyRpcStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyStatusStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyTypeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyTypedefStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyUniqueStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyUnitsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyUsesStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyValueStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyWhenStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyYangVersionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.EmptyYinElementStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.ImportStatementImpl;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.ModuleStatementImpl;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RefineStatementImpl;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularActionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularAnydataStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularAnyxmlStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularArgumentStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularAugmentStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularBaseStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularBelongsToStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularBitStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularChoiceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularConfigStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularContactStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularContainerStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularDefaultStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularDescriptionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularEnumStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularExtensionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularFeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularFractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularGroupingStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularIdentityStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularIfFeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularIncludeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularInputStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularKeyStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularLeafListStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularLeafStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularLengthStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularListStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularMandatoryStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularMaxElementsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularMinElementsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularModifierStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularMustStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularNamespaceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularNotificationStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularOrderedByStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularOrganizationStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularOutputStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularPathStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularPatternStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularPositionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularPrefixStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularPresenceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularRangeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularReferenceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularRevisionDateStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularRevisionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularRpcStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularStatusStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularTypeStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularTypedefStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularUniqueStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularUnitsStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularUsesStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularValueStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularWhenStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularYangVersionStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.RegularYinElementStatement;
import org.opendaylight.yangtools.yang.model.spi.stmt.impl.decl.SubmoduleStatementImpl;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Static entry point to instantiating {@link DeclaredStatement} covered in the {@code RFC7950} metamodel.
 */
@Beta
@NonNullByDefault
public final class DeclaredStatements {
    private DeclaredStatements() {
        // Hidden on purpose
    }

    public static ActionStatement createAction(final QName argument) {
        return new EmptyActionStatement(argument);
    }

    public static ActionStatement createAction(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createAction(argument) : new RegularActionStatement(argument, substatements);
    }

    public static AnydataStatement createAnydata(final QName argument) {
        return new EmptyAnydataStatement(argument);
    }

    public static AnydataStatement createAnydata(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createAnydata(argument) : new RegularAnydataStatement(argument, substatements);
    }

    public static AnyxmlStatement createAnyxml(final QName argument) {
        return new EmptyAnyxmlStatement(argument);
    }

    public static AnyxmlStatement createAnyxml(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createAnyxml(argument) : new RegularAnyxmlStatement(argument, substatements);
    }

    public static ArgumentStatement createArgument(final QName argument) {
        return new EmptyArgumentStatement(argument);
    }

    public static ArgumentStatement createArgument(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createArgument(argument)
            : new RegularArgumentStatement(argument, substatements);
    }

    public static AugmentStatement createAugment(final String rawArgument, final SchemaNodeIdentifier argument) {
        return new EmptyAugmentStatement(rawArgument, argument);
    }

    public static AugmentStatement createAugment(final String rawArgument, final SchemaNodeIdentifier argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createAugment(rawArgument, argument)
            : new RegularAugmentStatement(rawArgument, argument, substatements);
    }

    public static BaseStatement createBase(final QName argument) {
        return new EmptyBaseStatement(argument);
    }

    public static BaseStatement createBase(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createBase(argument) : new RegularBaseStatement(argument, substatements);
    }

    public static BelongsToStatement createBelongsTo(final String argument) {
        return new EmptyBelongsToStatement(argument);
    }

    public static BelongsToStatement createBelongsTo(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createBelongsTo(argument)
            : new RegularBelongsToStatement(argument, substatements);
    }

    public static BitStatement createBit(final String argument) {
        return new EmptyBitStatement(argument);
    }

    public static BitStatement createBit(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createBit(argument) : new RegularBitStatement(argument, substatements);
    }

    public static ChoiceStatement createChoice(final QName argument) {
        return new EmptyChoiceStatement(argument);
    }

    public static ChoiceStatement createChoice(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createChoice(argument) : new RegularChoiceStatement(argument, substatements);
    }

    public static ConfigStatement createConfig(final Boolean argument) {
        return new EmptyConfigStatement(argument);
    }

    public static ConfigStatement createConfig(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createConfig(argument) : new RegularConfigStatement(argument, substatements);
    }

    public static ContactStatement createContact(final String argument) {
        return new EmptyContactStatement(argument);
    }

    public static ContactStatement createContact(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createContact(argument) : new RegularContactStatement(argument, substatements);
    }

    public static ContainerStatement createContainer(final QName argument) {
        return new EmptyContainerStatement(argument);
    }

    public static ContainerStatement createContainer(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createContainer(argument)
            : new RegularContainerStatement(argument, substatements);
    }

    public static DefaultStatement createDefault(final String argument) {
        return new EmptyDefaultStatement(argument);
    }

    public static DefaultStatement createDefault(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createDefault(argument) : new RegularDefaultStatement(argument, substatements);
    }

    public static DescriptionStatement createDescription(final String argument) {
        return new EmptyDescriptionStatement(argument);
    }

    public static DescriptionStatement createDescription(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createDescription(argument)
            : new RegularDescriptionStatement(argument, substatements);
    }

    public static DeviateStatement createDeviate(final DeviateKind argument) {
        // This is exceedingly unlikely, just reuse the implementation
        return createDeviate(argument, ImmutableList.of());
    }

    public static DeviateStatement createDeviate(final DeviateKind argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DeviateStatementImpl(argument, substatements);
    }

    public static DeviationStatement createDeviation(final String rawArgument, final Absolute argument) {
        // This does not make really sense
        return createDeviation(rawArgument, argument, ImmutableList.of());
    }

    public static DeviationStatement createDeviation(final String rawArgument, final Absolute argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DeviationStatementImpl(rawArgument, argument, substatements);
    }

    // FIXME: what is the distinction between rawArgument and argument?
    public static EnumStatement createEnum(final String rawArgument, final String argument) {
        return new EmptyEnumStatement(rawArgument, argument);
    }

    public static EnumStatement createEnum(final String rawArgument, final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createEnum(rawArgument, argument)
            : new RegularEnumStatement(rawArgument, argument, substatements);
    }

    public static ErrorAppTagStatement createErrorAppTag(final String argument) {
        return new EmptyErrorAppTagStatement(argument);
    }

    public static ErrorAppTagStatement createErrorAppTag(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createErrorAppTag(argument)
            : new RegularErrorAppTagStatement(argument, substatements);
    }

    public static ErrorMessageStatement createErrorMessage(final String argument) {
        return new EmptyErrorMessageStatement(argument);
    }

    public static ErrorMessageStatement createErrorMessage(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createErrorMessage(argument)
            : new RegularErrorMessageStatement(argument, substatements);
    }

    public static ExtensionStatement createExtension(final QName argument) {
        return new EmptyExtensionStatement(argument);
    }

    public static ExtensionStatement createExtension(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createExtension(argument)
            : new RegularExtensionStatement(argument, substatements);
    }

    public static FeatureStatement createFeature(final QName argument) {
        return new EmptyFeatureStatement(argument);
    }

    public static FeatureStatement createFeature(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createFeature(argument) : new RegularFeatureStatement(argument, substatements);
    }

    public static FractionDigitsStatement createFractionDigits(final Integer argument) {
        return new EmptyFractionDigitsStatement(argument);
    }

    public static FractionDigitsStatement createFractionDigits(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createFractionDigits(argument)
            : new RegularFractionDigitsStatement(argument, substatements);
    }

    public static GroupingStatement createGrouping(final QName argument) {
        return new EmptyGroupingStatement(argument);
    }

    public static GroupingStatement createGrouping(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createGrouping(argument)
            : new RegularGroupingStatement(argument, substatements);
    }

    public static IdentityStatement createIdentity(final QName argument) {
        return new EmptyIdentityStatement(argument);
    }

    public static IdentityStatement createIdentity(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createIdentity(argument)
            : new RegularIdentityStatement(argument, substatements);
    }

    public static IfFeatureStatement createIfFeature(final String rawArgument, final IfFeatureExpr argument) {
        return new EmptyIfFeatureStatement(rawArgument, argument);
    }

    public static IfFeatureStatement createIfFeature(final String rawArgument, final IfFeatureExpr argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createIfFeature(rawArgument, argument)
            : new RegularIfFeatureStatement(rawArgument, argument, substatements);
    }

    public static ImportStatement createImport(final String argument) {
        // This should never happen
        return createImport(argument, ImmutableList.of());
    }

    public static ImportStatement createImport(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ImportStatementImpl(argument, substatements);
    }

    public static IncludeStatement createInclude(final String rawArgument, final String argument) {
        return new EmptyIncludeStatement(rawArgument, argument);
    }

    public static IncludeStatement createInclude(final String rawArgument, final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createInclude(rawArgument, argument)
            : new RegularIncludeStatement(rawArgument, argument, substatements);
    }

    public static InputStatement createInput(final QName argument) {
        return new EmptyInputStatement(argument);
    }

    public static InputStatement createInput(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createInput(argument) : new RegularInputStatement(argument, substatements);
    }

    public static KeyStatement createKey(final String rawArgument, final Set<QName> argument) {
        return new EmptyKeyStatement(rawArgument, argument);
    }

    public static KeyStatement createKey(final String rawArgument, final Set<QName> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createKey(rawArgument, argument)
            : new RegularKeyStatement(rawArgument, argument, substatements);
    }

    public static LeafStatement createLeaf(final QName argument) {
        return new EmptyLeafStatement(argument);
    }

    public static LeafStatement createLeaf(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createLeaf(argument) : new RegularLeafStatement(argument, substatements);
    }

    public static LeafListStatement createLeafList(final QName argument) {
        return new EmptyLeafListStatement(argument);
    }

    public static LeafListStatement createLeafList(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createLeafList(argument)
            : new RegularLeafListStatement(argument, substatements);
    }

    public static LengthStatement createLength(final String rawArgument, final List<ValueRange> argument) {
        return new EmptyLengthStatement(rawArgument, argument);
    }

    public static LengthStatement createLength(final String rawArgument, final List<ValueRange> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createLength(rawArgument, argument)
            : new RegularLengthStatement(rawArgument, argument, substatements);
    }

    public static ListStatement createList(final QName argument) {
        return new EmptyListStatement(argument);
    }

    public static ListStatement createList(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createList(argument) : new RegularListStatement(argument, substatements);
    }

    public static MandatoryStatement createMandatory(final Boolean argument) {
        return new EmptyMandatoryStatement(argument);
    }

    public static MandatoryStatement createMandatory(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createMandatory(argument)
            : new RegularMandatoryStatement(argument, substatements);
    }

    public static MaxElementsStatement createMaxElements(final String rawArgument, final String argument) {
        return new EmptyMaxElementsStatement(rawArgument, argument);
    }

    public static MaxElementsStatement createMaxElements(final String rawArgument, final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createMaxElements(rawArgument, argument)
            : new RegularMaxElementsStatement(rawArgument, argument, substatements);
    }

    public static MinElementsStatement createMinElements(final Integer argument) {
        return new EmptyMinElementsStatement(argument);
    }

    public static MinElementsStatement createMinElements(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createMinElements(argument)
            : new RegularMinElementsStatement(argument, substatements);
    }

    public static ModifierStatement createModifier(final ModifierKind argument) {
        return new EmptyModifierStatement(argument);
    }

    public static ModifierStatement createModifier(final ModifierKind argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createModifier(argument)
            : new RegularModifierStatement(argument, substatements);
    }

    public static ModuleStatement createModule(final String rawArgument, final UnqualifiedQName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ModuleStatementImpl(rawArgument, argument, substatements);
    }

    public static MustStatement createMust(final String rawArgument, final QualifiedBound argument) {
        return new EmptyMustStatement(rawArgument, argument);
    }

    public static MustStatement createMust(final String rawArgument, final QualifiedBound argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createMust(rawArgument, argument)
            : new RegularMustStatement(rawArgument, argument, substatements);
    }

    public static NamespaceStatement createNamespace(final XMLNamespace argument) {
        return new EmptyNamespaceStatement(argument);
    }

    public static NamespaceStatement createNamespace(final XMLNamespace argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createNamespace(argument)
            : new RegularNamespaceStatement(argument, substatements);
    }

    public static NotificationStatement createNotification(final QName argument) {
        return new EmptyNotificationStatement(argument);
    }

    public static NotificationStatement createNotification(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createNotification(argument)
            : new RegularNotificationStatement(argument, substatements);
    }

    public static OrganizationStatement createOrganization(final String argument) {
        return new EmptyOrganizationStatement(argument);
    }

    public static OrganizationStatement createOrganization(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createOrganization(argument)
            : new RegularOrganizationStatement(argument, substatements);
    }

    public static OutputStatement createOutput(final QName argument) {
        return new EmptyOutputStatement(argument);
    }

    public static OutputStatement createOutput(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createOutput(argument) : new RegularOutputStatement(argument, substatements);
    }

    public static OrderedByStatement createOrderedBy(final Ordering argument) {
        return new EmptyOrderedByStatement(argument);
    }

    public static OrderedByStatement createOrderedBy(final Ordering argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createOrderedBy(argument)
            : new RegularOrderedByStatement(argument, substatements);
    }

    public static PathStatement createPath(final PathExpression argument) {
        return new EmptyPathStatement(argument);
    }

    public static PathStatement createPath(final PathExpression argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createPath(argument) : new RegularPathStatement(argument, substatements);
    }

    public static PatternStatement createPattern(final PatternExpression argument) {
        return new EmptyPatternStatement(argument);
    }

    public static PatternStatement createPattern(final PatternExpression argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createPattern(argument)
            : new RegularPatternStatement(argument, substatements);
    }

    public static PositionStatement createPosition(final Uint32 argument) {
        return new EmptyPositionStatement(argument);
    }

    public static PositionStatement createPosition(final Uint32 argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createPosition(argument)
            : new RegularPositionStatement(argument, substatements);
    }

    public static PrefixStatement createPrefix(final String argument) {
        return new EmptyPrefixStatement(argument);
    }

    public static PrefixStatement createPrefix(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createPrefix(argument) : new RegularPrefixStatement(argument, substatements);
    }

    public static PresenceStatement createPresence(final String argument) {
        return new EmptyPresenceStatement(argument);
    }

    public static PresenceStatement createPresence(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createPresence(argument)
            : new RegularPresenceStatement(argument, substatements);
    }

    public static ReferenceStatement createReference(final String argument) {
        return new EmptyReferenceStatement(argument);
    }

    public static ReferenceStatement createReference(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createReference(argument)
            : new RegularReferenceStatement(argument, substatements);
    }

    public static RangeStatement createRange(final String rawArgument, final List<ValueRange> argument) {
        return new EmptyRangeStatement(rawArgument, argument);
    }

    public static RangeStatement createRange(final String rawArgument, final List<ValueRange> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createRange(rawArgument, argument)
            : new RegularRangeStatement(rawArgument, argument, substatements);
    }

    public static RefineStatement createRefine(final String rawArgument, final Descendant argument) {
        return createRefine(rawArgument, argument, ImmutableList.of());
    }

    public static RefineStatement createRefine(final String rawArgument, final Descendant argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RefineStatementImpl(rawArgument, argument, substatements);
    }

    public static RequireInstanceStatement createRequireInstance(final Boolean argument) {
        return new EmptyRequireInstanceStatement(argument);
    }

    public static RequireInstanceStatement createRequireInstance(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createRequireInstance(argument)
            : new RegularRequireInstanceStatement(argument, substatements);
    }

    public static RevisionStatement createRevision(final Revision argument) {
        return new EmptyRevisionStatement(argument);
    }

    public static RevisionStatement createRevision(final Revision argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createRevision(argument)
            : new RegularRevisionStatement(argument, substatements);
    }

    public static RevisionDateStatement createRevisionDate(final Revision argument) {
        return new EmptyRevisionDateStatement(argument);
    }

    public static RevisionDateStatement createRevisionDate(final Revision argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createRevisionDate(argument)
            : new RegularRevisionDateStatement(argument, substatements);
    }

    public static RpcStatement createRpc(final QName argument) {
        return new EmptyRpcStatement(argument);
    }

    public static RpcStatement createRpc(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createRpc(argument) : new RegularRpcStatement(argument, substatements);
    }

    public static StatusStatement createStatus(final Status argument) {
        return new EmptyStatusStatement(argument);
    }

    public static StatusStatement createStatus(final Status argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createStatus(argument) : new RegularStatusStatement(argument, substatements);
    }

    public static SubmoduleStatement createSubmodule(final String rawArgument, final UnqualifiedQName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new SubmoduleStatementImpl(rawArgument, argument, substatements);
    }

    public static TypeStatement createType(final String argument) {
        return new EmptyTypeStatement(argument);
    }

    public static TypeStatement createType(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createType(argument) : new RegularTypeStatement(argument, substatements);
    }

    public static TypedefStatement createTypedef(final QName argument) {
        return new EmptyTypedefStatement(argument);
    }

    public static TypedefStatement createTypedef(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createTypedef(argument) : new RegularTypedefStatement(argument, substatements);
    }

    public static UniqueStatement createUnique(final String rawArgument, final Set<Descendant> argument) {
        return new EmptyUniqueStatement(rawArgument, argument);
    }

    public static UniqueStatement createUnique(final String rawArgument, final Set<Descendant> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createUnique(rawArgument, argument)
            : new RegularUniqueStatement(rawArgument, argument, substatements);
    }

    public static UnitsStatement createUnits(final String argument) {
        return new EmptyUnitsStatement(argument);
    }

    public static UnitsStatement createUnits(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createUnits(argument) : new RegularUnitsStatement(argument, substatements);
    }

    public static UsesStatement createUses(final String rawArgument, final QName argument) {
        return new EmptyUsesStatement(rawArgument, argument);
    }

    public static UsesStatement createUses(final String rawArgument, final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createUses(rawArgument, argument)
            : new RegularUsesStatement(rawArgument, argument, substatements);
    }

    public static ValueStatement createValue(final Integer argument) {
        return new EmptyValueStatement(argument);
    }

    public static ValueStatement createValue(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createValue(argument) : new RegularValueStatement(argument, substatements);
    }

    public static WhenStatement createWhen(final String rawArgument, final QualifiedBound argument) {
        return new EmptyWhenStatement(rawArgument, argument);
    }

    public static WhenStatement createWhen(final String rawArgument, final QualifiedBound argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createWhen(rawArgument, argument)
            : new RegularWhenStatement(rawArgument, argument, substatements);
    }

    public static YangVersionStatement createYangVersion(final YangVersion argument) {
        return new EmptyYangVersionStatement(argument);
    }

    public static YangVersionStatement createYangVersion(final YangVersion argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createYangVersion(argument)
            : new RegularYangVersionStatement(argument, substatements);
    }

    public static YinElementStatement createYinElement(final Boolean argument) {
        return new EmptyYinElementStatement(argument);
    }

    public static YinElementStatement createYinElement(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createYinElement(argument)
            : new RegularYinElementStatement(argument, substatements);
    }
}
