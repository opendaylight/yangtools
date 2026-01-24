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
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefActionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefAnydataStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefAnyxmlStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefArgumentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefAugmentStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefBaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefBelongsToStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefBinaryTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefBitStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefBitsTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefCaseStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefChoiceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefConfigStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefContactStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefContainerStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDecimal64TypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDefaultStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDescriptionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDeviateStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefDeviationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefEnumStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefEnumTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefExtensionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefFractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefGroupingStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefIdentityStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefIdentityrefTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefIfFeatureStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefImportStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefIncludeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefInputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefInstanceIdentifierTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefKeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefLeafListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefLeafStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefLeafrefTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefLengthStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefMandatoryStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefMaxElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefMinElementsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefModifierStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefModuleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefMustStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefNamespaceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefNotificationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefNumericaTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefOrderedByStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefOrganizationStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefOutputStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefPathStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefPatternStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefPositionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefPrefixStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefPresenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRangeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefReferenceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRefineStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRevisionDateStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRevisionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefRpcStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefStatusStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefStringTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefSubmoduleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefTypedefStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefUnionTypeStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefUniqueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefUnitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefUnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefUsesStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefValueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefWhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefYangVersionStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.impl.ref.RefYinElementStatement;

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

    public static ActionStatement decorateAction(final ActionStatement stmt, final DeclarationReference ref) {
        return new RefActionStatement(stmt, ref);
    }

    public static AnydataStatement decorateAnydata(final AnydataStatement stmt, final DeclarationReference ref) {
        return new RefAnydataStatement(stmt, ref);
    }

    public static AnyxmlStatement decorateAnyxml(final AnyxmlStatement stmt, final DeclarationReference ref) {
        return new RefAnyxmlStatement(stmt, ref);
    }

    public static ArgumentStatement decorateArgument(final ArgumentStatement stmt, final DeclarationReference ref) {
        return new RefArgumentStatement(stmt, ref);
    }

    public static AugmentStatement decorateAugment(final AugmentStatement stmt, final DeclarationReference ref) {
        return new RefAugmentStatement(stmt, ref);
    }

    public static BaseStatement decorateBase(final BaseStatement stmt, final DeclarationReference ref) {
        return new RefBaseStatement(stmt, ref);
    }

    public static BelongsToStatement decorateBelongsTo(final BelongsToStatement stmt, final DeclarationReference ref) {
        return new RefBelongsToStatement(stmt, ref);
    }

    public static BitStatement decorateBit(final BitStatement stmt, final DeclarationReference ref) {
        return new RefBitStatement(stmt, ref);
    }

    public static CaseStatement decorateCase(final CaseStatement stmt, final DeclarationReference ref) {
        return new RefCaseStatement(stmt, ref);
    }

    public static ChoiceStatement decorateChoice(final ChoiceStatement stmt, final DeclarationReference ref) {
        return new RefChoiceStatement(stmt, ref);
    }

    public static ConfigStatement decorateConfig(final ConfigStatement stmt, final DeclarationReference ref) {
        return new RefConfigStatement(stmt, ref);
    }

    public static ContactStatement decorateContact(final ContactStatement stmt, final DeclarationReference ref) {
        return new RefContactStatement(stmt, ref);
    }

    public static ContainerStatement decorateContainer(final ContainerStatement stmt, final DeclarationReference ref) {
        return new RefContainerStatement(stmt, ref);
    }

    public static DefaultStatement decorateDefault(final DefaultStatement stmt, final DeclarationReference ref) {
        return new RefDefaultStatement(stmt, ref);
    }

    public static DescriptionStatement decorateDescription(final DescriptionStatement stmt,
            final DeclarationReference ref) {
        return new RefDescriptionStatement(stmt, ref);
    }

    public static DeviateStatement decorateDeviate(final DeviateStatement stmt, final DeclarationReference ref) {
        return new RefDeviateStatement(stmt, ref);
    }

    public static DeviationStatement decorateDeviation(final DeviationStatement stmt, final DeclarationReference ref) {
        return new RefDeviationStatement(stmt, ref);
    }

    public static EnumStatement decorateEnum(final EnumStatement stmt, final DeclarationReference ref) {
        return new RefEnumStatement(stmt, ref);
    }

    public static ErrorAppTagStatement decorateErrorAppTag(final ErrorAppTagStatement stmt,
            final DeclarationReference ref) {
        return new RefErrorAppTagStatement(stmt, ref);
    }

    public static ErrorMessageStatement decorateErrorMessage(final ErrorMessageStatement stmt,
            final DeclarationReference ref) {
        return new RefErrorMessageStatement(stmt, ref);
    }

    public static ExtensionStatement decorateExtesion(final ExtensionStatement stmt, final DeclarationReference ref) {
        return new RefExtensionStatement(stmt, ref);
    }

    public static FeatureStatement decorateFeature(final FeatureStatement stmt, final DeclarationReference ref) {
        return new RefFeatureStatement(stmt, ref);
    }

    public static FractionDigitsStatement decorateFractionDigits(final FractionDigitsStatement stmt,
            final DeclarationReference ref) {
        return new RefFractionDigitsStatement(stmt, ref);
    }

    public static GroupingStatement decorateGrouping(final GroupingStatement stmt, final DeclarationReference ref) {
        return new RefGroupingStatement(stmt, ref);
    }

    public static IdentityStatement decorateIdentity(final IdentityStatement stmt, final DeclarationReference ref) {
        return new RefIdentityStatement(stmt, ref);
    }

    public static IfFeatureStatement decorateIfFeature(final IfFeatureStatement stmt, final DeclarationReference ref) {
        return new RefIfFeatureStatement(stmt, ref);
    }

    public static ImportStatement decorateImport(final ImportStatement stmt, final DeclarationReference ref) {
        return new RefImportStatement(stmt, ref);
    }

    public static IncludeStatement decorateInclude(final IncludeStatement stmt, final DeclarationReference ref) {
        return new RefIncludeStatement(stmt, ref);
    }

    public static InputStatement decorateInput(final InputStatement stmt, final DeclarationReference ref) {
        return new RefInputStatement(stmt, ref);
    }

    public static KeyStatement decorateKey(final KeyStatement stmt, final DeclarationReference ref) {
        return new RefKeyStatement(stmt, ref);
    }

    public static LeafStatement decorateLeaf(final LeafStatement stmt, final DeclarationReference ref) {
        return new RefLeafStatement(stmt, ref);
    }

    public static LeafListStatement decorateLeafList(final LeafListStatement stmt, final DeclarationReference ref) {
        return new RefLeafListStatement(stmt, ref);
    }

    public static LengthStatement decorateLength(final LengthStatement stmt, final DeclarationReference ref) {
        return new RefLengthStatement(stmt, ref);
    }

    public static ListStatement decorateList(final ListStatement stmt, final DeclarationReference ref) {
        return new RefListStatement(stmt, ref);
    }

    public static MandatoryStatement decorateMandatory(final MandatoryStatement stmt, final DeclarationReference ref) {
        return new RefMandatoryStatement(stmt, ref);
    }

    public static MaxElementsStatement decorateMaxElements(final MaxElementsStatement stmt,
            final DeclarationReference ref) {
        return new RefMaxElementsStatement(stmt, ref);
    }

    public static MinElementsStatement decorateMinElements(final MinElementsStatement stmt,
            final DeclarationReference ref) {
        return new RefMinElementsStatement(stmt, ref);
    }

    public static ModifierStatement decorateModifier(final ModifierStatement stmt, final DeclarationReference ref) {
        return new RefModifierStatement(stmt, ref);
    }

    public static ModuleStatement decorateModule(final ModuleStatement stmt, final DeclarationReference ref) {
        return new RefModuleStatement(stmt, ref);
    }

    public static MustStatement decorateMust(final MustStatement stmt, final DeclarationReference ref) {
        return new RefMustStatement(stmt, ref);
    }

    public static NamespaceStatement decorateNamespace(final NamespaceStatement stmt, final DeclarationReference ref) {
        return new RefNamespaceStatement(stmt, ref);
    }

    public static NotificationStatement decorateNotification(final NotificationStatement stmt,
            final DeclarationReference ref) {
        return new RefNotificationStatement(stmt, ref);
    }

    public static OrderedByStatement decorateOrderedBy(final OrderedByStatement stmt,
            final DeclarationReference ref) {
        return new RefOrderedByStatement(stmt, ref);
    }

    public static OrganizationStatement decorateOrganization(final OrganizationStatement stmt,
            final DeclarationReference ref) {
        return new RefOrganizationStatement(stmt, ref);
    }

    public static OutputStatement decorateOutput(final OutputStatement stmt, final DeclarationReference ref) {
        return new RefOutputStatement(stmt, ref);
    }

    public static PathStatement decoratePath(final PathStatement stmt, final DeclarationReference ref) {
        return new RefPathStatement(stmt, ref);
    }

    public static PatternStatement decoratePattern(final PatternStatement stmt, final DeclarationReference ref) {
        return new RefPatternStatement(stmt, ref);
    }

    public static PositionStatement decoratePosition(final PositionStatement stmt, final DeclarationReference ref) {
        return new RefPositionStatement(stmt, ref);
    }

    public static PrefixStatement decoratePrefix(final PrefixStatement stmt, final DeclarationReference ref) {
        return new RefPrefixStatement(stmt, ref);
    }

    public static PresenceStatement decoratePresence(final PresenceStatement stmt,
            final DeclarationReference ref) {
        return new RefPresenceStatement(stmt, ref);
    }

    public static RangeStatement decorateRange(final RangeStatement stmt, final DeclarationReference ref) {
        return new RefRangeStatement(stmt, ref);
    }

    public static ReferenceStatement decorateReference(final ReferenceStatement stmt, final DeclarationReference ref) {
        return new RefReferenceStatement(stmt, ref);
    }

    public static RefineStatement decorateRefine(final RefineStatement stmt, final DeclarationReference ref) {
        return new RefRefineStatement(stmt, ref);
    }

    public static RequireInstanceStatement decorateRequireInstance(final RequireInstanceStatement stmt,
            final DeclarationReference ref) {
        return new RefRequireInstanceStatement(stmt, ref);
    }

    public static RevisionStatement decorateRevision(final RevisionStatement stmt, final DeclarationReference ref) {
        return new RefRevisionStatement(stmt, ref);
    }

    public static RevisionDateStatement decorateRevisionDate(final RevisionDateStatement stmt,
            final DeclarationReference ref) {
        return new RefRevisionDateStatement(stmt, ref);
    }

    public static RpcStatement decorateRpc(final RpcStatement stmt, final DeclarationReference ref) {
        return new RefRpcStatement(stmt, ref);
    }

    public static StatusStatement decorateStatus(final StatusStatement stmt, final DeclarationReference ref) {
        return new RefStatusStatement(stmt, ref);
    }

    public static SubmoduleStatement decorateSubmodule(final SubmoduleStatement stmt, final DeclarationReference ref) {
        return new RefSubmoduleStatement(stmt, ref);
    }

    public static TypeStatement decorateType(final TypeStatement stmt, final DeclarationReference ref) {
        return switch (stmt) {
            case TypeStatement.OfBinary type -> new RefBinaryTypeStatement(type, ref);
            case TypeStatement.OfBits type -> new RefBitsTypeStatement(type, ref);
            case TypeStatement.OfDecimal64 type -> new RefDecimal64TypeStatement(type, ref);
            case TypeStatement.OfEnum type -> new RefEnumTypeStatement(type, ref);
            case TypeStatement.OfIdentityref type -> new RefIdentityrefTypeStatement(type, ref);
            case TypeStatement.OfInstanceIdentifier type -> new RefInstanceIdentifierTypeStatement(type, ref);
            case TypeStatement.OfLeafref type -> new RefLeafrefTypeStatement(type, ref);
            case TypeStatement.OfNumerical type -> new RefNumericaTypeStatement(type, ref);
            case TypeStatement.OfString type -> new RefStringTypeStatement(type, ref);
            case TypeStatement.OfUnion type -> new RefUnionTypeStatement(type, ref);
        };
    }

    public static TypedefStatement decorateTypedef(final TypedefStatement stmt, final DeclarationReference ref) {
        return new RefTypedefStatement(stmt, ref);
    }

    public static UniqueStatement decorateUnique(final UniqueStatement stmt, final DeclarationReference ref) {
        return new RefUniqueStatement(stmt, ref);
    }

    public static UnitsStatement decorateUnits(final UnitsStatement stmt, final DeclarationReference ref) {
        return new RefUnitsStatement(stmt, ref);
    }

    public static UnrecognizedStatement decorateUnrecognized(final UnrecognizedStatement stmt,
            final DeclarationReference ref) {
        return new RefUnrecognizedStatement(stmt, ref);
    }

    public static UsesStatement decorateUses(final UsesStatement stmt, final DeclarationReference ref) {
        return new RefUsesStatement(stmt, ref);
    }

    public static ValueStatement decorateValue(final ValueStatement stmt, final DeclarationReference ref) {
        return new RefValueStatement(stmt, ref);
    }

    public static WhenStatement decorateWhen(final WhenStatement stmt, final DeclarationReference ref) {
        return new RefWhenStatement(stmt, ref);
    }

    public static YangVersionStatement decorateYangVersion(final YangVersionStatement stmt,
            final DeclarationReference ref) {
        return new RefYangVersionStatement(stmt, ref);
    }

    public static YinElementStatement decorateYinElement(final YinElementStatement stmt,
            final DeclarationReference ref) {
        return new RefYinElementStatement(stmt, ref);
    }
}
