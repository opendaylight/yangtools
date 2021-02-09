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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;

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

    public static StatusStatement createStatus(final Status argument) {
        return new EmptyStatusStatement(argument);
    }

    public static StatusStatement createStatus(final Status argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? createStatus(argument) : new RegularStatusStatement(argument, substatements);
    }
}
