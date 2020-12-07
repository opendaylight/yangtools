/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDeviateStatement;

/**
 * Static entry point to enriching {@link DeclaredStatement}s covered in the {@code RFC7950} metamodel with
 * {@link DeclarationReference}s.
 */
@Beta
@NonNullByDefault
public final class DeclaredStatementDecorators {
    private DeclaredStatementDecorators() {
        // Hidden on purpose
    }

    public static ActionStatement decorateAction(final ActionStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static AnydataStatement decorateAnydata(final AnydataStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static AnyxmlStatement decorateAnyxml(final AnyxmlStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ArgumentStatement decorateArgument(final ArgumentStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static AugmentStatement decorateAugment(final AugmentStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static BaseStatement decorateBase(final BaseStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static BelongsToStatement decorateBelongsTo(final BelongsToStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static BitStatement decorateBit(final BitStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static CaseStatement decorateCase(final CaseStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ChoiceStatement decorateChoice(final ChoiceStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ConfigStatement decorateConfig(final ConfigStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ContactStatement decorateContact(final ContactStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ContainerStatement decorateContainer(final ContainerStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static DefaultStatement decorateDefault(final DefaultStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static DescriptionStatement decorateDescription(final DescriptionStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static DeviateStatement decorateDeviate(final DeviateStatement stmt, final DeclarationReference ref) {
        return new RefDeviateStatement(stmt, ref);
    }

    public static DeviationStatement decorateDeviation(final DeviationStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static EnumStatement decorateEnum(final EnumStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ErrorAppTagStatement decorateErrorAppTag(final ErrorAppTagStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ErrorMessageStatement decorateErrorMessage(final ErrorMessageStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static FeatureStatement decorateFeature(final FeatureStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static FractionDigitsStatement decorateFractionDigits(final FractionDigitsStatement stmt,
        final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static GroupingStatement decorateGrouping(final GroupingStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static IfFeatureStatement decorateIfFeature(final IfFeatureStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ImportStatement decorateImport(final ImportStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static IncludeStatement decorateInclude(final IncludeStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static InputStatement decorateInput(final InputStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static KeyStatement decorateKey(final KeyStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static LeafStatement decorateLeaf(final LeafStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static LeafListStatement decorateLeafList(final LeafListStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static MandatoryStatement decorateMandatory(final MandatoryStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static MaxElementsStatement decorateMaxElements(final MaxElementsStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static MinElementsStatement decorateMinElements(final MinElementsStatement stmt,
            final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static ValueStatement decorateValue(final ValueStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }

    public static WhenStatement decorateWhen(final WhenStatement stmt, final DeclarationReference reference) {
        // TODO Auto-generated method stub
        return null;
    }
}
