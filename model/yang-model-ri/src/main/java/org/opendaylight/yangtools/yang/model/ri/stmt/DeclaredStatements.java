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
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsValue;
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
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRange;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.DeviateStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.DeviationStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyActionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyAnydataStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyAnyxmlStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyArgumentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyAugmentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyBaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyBelongsToStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyBitStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyCaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyChoiceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyConfigStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyContactStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyContainerStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyDefaultStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyDescriptionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyEnumStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyExtensionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyFractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyGroupingStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyIdentityStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyIfFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyIncludeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyInputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyKeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyLeafListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyLeafStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyLengthStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyMandatoryStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyMaxElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyMinElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyModifierStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyMustStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyNamespaceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyNotificationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyOrderedByStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyOrganizationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyOutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyPathStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyPatternStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyPositionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyPrefixStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyPresenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRangeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyReferenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRevisionDateStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRevisionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyRpcStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyStatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyTypedefStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUniqueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUnitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyUsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyValueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyWhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyYangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.EmptyYinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.ImportStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.ModuleStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RefineStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularActionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularAnydataStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularAnyxmlStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularArgumentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularAugmentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularBaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularBelongsToStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularBitStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularCaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularChoiceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularConfigStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularContactStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularContainerStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularDefaultStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularDescriptionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularEnumStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularExtensionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularFractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularGroupingStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularIdentityStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularIfFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularIncludeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularInputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularKeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularLeafListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularLeafStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularLengthStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularMandatoryStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularMaxElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularMinElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularModifierStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularMustStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularNamespaceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularNotificationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularOrderedByStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularOrganizationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularOutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularPathStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularPatternStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularPositionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularPrefixStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularPresenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularRangeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularReferenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularRevisionDateStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularRevisionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularRpcStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularStatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularTypedefStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUniqueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUnitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularUsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularValueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularWhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularYangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.RegularYinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.SubmoduleStatementImpl;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl.UnrecognizedStatementImpl;
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

    public static ActionStatement createAction(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyActionStatement(argument)
            : new RegularActionStatement(argument, substatements);
    }

    public static AnydataStatement createAnydata(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyAnydataStatement(argument)
            : new RegularAnydataStatement(argument, substatements);
    }

    public static AnyxmlStatement createAnyxml(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyAnyxmlStatement(argument)
            : new RegularAnyxmlStatement(argument, substatements);
    }

    public static ArgumentStatement createArgument(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyArgumentStatement(argument)
            : new RegularArgumentStatement(argument, substatements);
    }

    public static AugmentStatement createAugment(final String rawArgument, final SchemaNodeIdentifier argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyAugmentStatement(rawArgument, argument)
            : new RegularAugmentStatement(rawArgument, argument, substatements);
    }

    public static BaseStatement createBase(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyBaseStatement(argument)
            : new RegularBaseStatement(argument, substatements);
    }

    public static BelongsToStatement createBelongsTo(final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyBelongsToStatement(argument)
            : new RegularBelongsToStatement(argument, substatements);
    }

    public static BitStatement createBit(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyBitStatement(argument)
            : new RegularBitStatement(argument, substatements);
    }

    public static CaseStatement createCase(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyCaseStatement(argument)
            : new RegularCaseStatement(argument, substatements);
    }

    public static ChoiceStatement createChoice(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyChoiceStatement(argument)
            : new RegularChoiceStatement(argument, substatements);
    }

    public static ConfigStatement createConfig(final boolean argument) {
        return argument ? EmptyConfigStatement.TRUE : EmptyConfigStatement.FALSE;
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

    public static ContainerStatement createContainer(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyContainerStatement(argument)
            : new RegularContainerStatement(argument, substatements);
    }

    public static DefaultStatement createDefault(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyDefaultStatement(argument)
            : new RegularDefaultStatement(argument, substatements);
    }

    public static DescriptionStatement createDescription(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyDescriptionStatement(argument)
            : new RegularDescriptionStatement(argument, substatements);
    }

    public static DeviateStatement createDeviate(final DeviateKind argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DeviateStatementImpl(argument, substatements);
    }

    public static DeviationStatement createDeviation(final String rawArgument, final Absolute argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DeviationStatementImpl(rawArgument, argument, substatements);
    }

    // FIXME: what is the distinction between rawArgument and argument?
    public static EnumStatement createEnum(final String rawArgument, final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyEnumStatement(rawArgument, argument)
            : new RegularEnumStatement(rawArgument, argument, substatements);
    }

    public static ErrorAppTagStatement createErrorAppTag(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorAppTagStatement(argument)
            : new RegularErrorAppTagStatement(argument, substatements);
    }

    public static ErrorMessageStatement createErrorMessage(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyErrorMessageStatement(argument)
            : new RegularErrorMessageStatement(argument, substatements);
    }

    public static ExtensionStatement createExtension(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyExtensionStatement(argument)
            : new RegularExtensionStatement(argument, substatements);
    }

    public static FeatureStatement createFeature(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyFeatureStatement(argument)
            : new RegularFeatureStatement(argument, substatements);
    }

    public static FractionDigitsStatement createFractionDigits(final int argument) {
        return EmptyFractionDigitsStatement.of(argument);
    }

    public static FractionDigitsStatement createFractionDigits(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createFractionDigits(argument)
            : new RegularFractionDigitsStatement(argument, substatements);
    }

    public static GroupingStatement createGrouping(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyGroupingStatement(argument)
            : new RegularGroupingStatement(argument, substatements);
    }

    public static IdentityStatement createIdentity(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyIdentityStatement(argument)
            : new RegularIdentityStatement(argument, substatements);
    }

    public static IfFeatureStatement createIfFeature(final String rawArgument, final IfFeatureExpr argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyIfFeatureStatement(rawArgument, argument)
            : new RegularIfFeatureStatement(rawArgument, argument, substatements);
    }

    public static ImportStatement createImport(final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ImportStatementImpl(argument, substatements);
    }

    public static IncludeStatement createInclude(final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyIncludeStatement(argument)
            : new RegularIncludeStatement(argument, substatements);
    }

    public static InputStatement createInput(final QName argument) {
        return new EmptyInputStatement(argument);
    }

    public static InputStatement createInput(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createInput(argument) : new RegularInputStatement(argument, substatements);
    }

    public static KeyStatement createKey(final String rawArgument, final Set<QName> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyKeyStatement(rawArgument, argument)
            : new RegularKeyStatement(rawArgument, argument, substatements);
    }

    public static LeafStatement createLeaf(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyLeafStatement(argument)
            : new RegularLeafStatement(argument, substatements);
    }

    public static LeafListStatement createLeafList(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyLeafListStatement(argument)
            : new RegularLeafListStatement(argument, substatements);
    }

    public static LengthStatement createLength(final String rawArgument, final List<ValueRange> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyLengthStatement(rawArgument, argument)
            : new RegularLengthStatement(rawArgument, argument, substatements);
    }

    public static ListStatement createList(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyListStatement(argument)
            : new RegularListStatement(argument, substatements);
    }

    public static MandatoryStatement createMandatory(final Boolean argument) {
        return new EmptyMandatoryStatement(argument);
    }

    public static MandatoryStatement createMandatory(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createMandatory(argument)
            : new RegularMandatoryStatement(argument, substatements);
    }

    public static MaxElementsStatement createMaxElements(final String rawArgument, final MaxElementsValue argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyMaxElementsStatement(rawArgument, argument)
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

    public static ModifierStatement createModifier(final ModifierKind argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyModifierStatement(argument)
            : new RegularModifierStatement(argument, substatements);
    }

    public static ModuleStatement createModule(final String rawArgument, final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ModuleStatementImpl(rawArgument, argument, substatements);
    }

    public static MustStatement createMust(final String rawArgument, final QualifiedBound argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyMustStatement(rawArgument, argument)
            : new RegularMustStatement(rawArgument, argument, substatements);
    }

    public static NamespaceStatement createNamespace(final XMLNamespace argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyNamespaceStatement(argument)
            : new RegularNamespaceStatement(argument, substatements);
    }

    public static NotificationStatement createNotification(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyNotificationStatement(argument)
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

    public static PathStatement createPath(final PathExpression argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyPathStatement(argument)
            : new RegularPathStatement(argument, substatements);
    }

    public static PatternStatement createPattern(final PatternExpression argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyPatternStatement(argument)
            : new RegularPatternStatement(argument, substatements);
    }

    public static PositionStatement createPosition(final Uint32 argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyPositionStatement(argument)
            : new RegularPositionStatement(argument, substatements);
    }

    public static PrefixStatement createPrefix(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyPrefixStatement(argument)
            : new RegularPrefixStatement(argument, substatements);
    }

    public static PresenceStatement createPresence(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyPresenceStatement(argument)
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

    public static RangeStatement createRange(final String rawArgument, final List<ValueRange> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyRangeStatement(rawArgument, argument)
            : new RegularRangeStatement(rawArgument, argument, substatements);
    }

    public static RefineStatement createRefine(final String rawArgument, final Descendant argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RefineStatementImpl(rawArgument, argument, substatements);
    }

    public static RequireInstanceStatement createRequireInstance(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            return argument ? EmptyRequireInstanceStatement.TRUE : EmptyRequireInstanceStatement.FALSE;
        }
        return new RegularRequireInstanceStatement(argument, substatements);
    }

    public static RevisionStatement createRevision(final Revision argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyRevisionStatement(argument)
            : new RegularRevisionStatement(argument, substatements);
    }

    public static RevisionDateStatement createRevisionDate(final Revision argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyRevisionDateStatement(argument)
            : new RegularRevisionDateStatement(argument, substatements);
    }

    public static RpcStatement createRpc(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyRpcStatement(argument)
            : new RegularRpcStatement(argument, substatements);
    }

    public static StatusStatement createStatus(final Status argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createStatus(argument) : new RegularStatusStatement(argument, substatements);
    }

    private static StatusStatement createStatus(final Status argument) {
        return switch (argument) {
            case CURRENT -> EmptyStatusStatement.CURRENT;
            case DEPRECATED -> EmptyStatusStatement.DEPRECATED;
            case OBSOLETE -> EmptyStatusStatement.OBSOLETE;
        };
    }

    public static SubmoduleStatement createSubmodule(final String rawArgument, final Unqualified argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new SubmoduleStatementImpl(rawArgument, argument, substatements);
    }

    public static TypeStatement createType(final String rawArgument, final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyTypeStatement(rawArgument, argument)
            : new RegularTypeStatement(rawArgument, argument, substatements);
    }

    public static TypedefStatement createTypedef(final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyTypedefStatement(argument)
            : new RegularTypedefStatement(argument, substatements);
    }

    public static UniqueStatement createUnique(final String rawArgument, final Set<Descendant> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyUniqueStatement(rawArgument, argument)
            : new RegularUniqueStatement(rawArgument, argument, substatements);
    }

    public static UnitsStatement createUnits(final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyUnitsStatement(argument)
            : new RegularUnitsStatement(argument, substatements);
    }

    public static UnrecognizedStatement createUnrecognized(final String rawArgument,
            final StatementDefinition publicDefinition,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new UnrecognizedStatementImpl(rawArgument, publicDefinition, substatements);
    }

    public static UsesStatement createUses(final String rawArgument, final QName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ?  new EmptyUsesStatement(rawArgument, argument)
            : new RegularUsesStatement(rawArgument, argument, substatements);
    }

    public static ValueStatement createValue(final Integer argument) {
        return new EmptyValueStatement(argument);
    }

    public static ValueStatement createValue(final Integer argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createValue(argument) : new RegularValueStatement(argument, substatements);
    }

    public static WhenStatement createWhen(final String rawArgument, final QualifiedBound argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? new EmptyWhenStatement(rawArgument, argument)
            : new RegularWhenStatement(rawArgument, argument, substatements);
    }

    public static YangVersionStatement createYangVersion(final YangVersion argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createYangVersion(argument)
            : new RegularYangVersionStatement(argument, substatements);
    }

    private static YangVersionStatement createYangVersion(final YangVersion argument) {
        return switch (argument) {
            case VERSION_1 -> EmptyYangVersionStatement.VERSION_1;
            case VERSION_1_1 -> EmptyYangVersionStatement.VERSION_1_1;
        };
    }

    public static YinElementStatement createYinElement(final boolean argument) {
        return argument ? EmptyYinElementStatement.TRUE : EmptyYinElementStatement.FALSE;
    }

    public static YinElementStatement createYinElement(final Boolean argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createYinElement(argument)
            : new RegularYinElementStatement(argument, substatements);
    }
}
