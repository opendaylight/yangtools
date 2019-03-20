/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;

/**
 * Mapping for both RFC6020 and RFC7950 statements.
 */
@Beta
@NonNullByDefault
public enum YangStmtMapping implements StatementDefinition {
    ACTION(ActionStatement.class, ActionEffectiveStatement.class, "action", "name"),
    ANYDATA(AnydataStatement.class, AnydataEffectiveStatement.class, "anydata", "name"),
    ANYXML(AnyxmlStatement.class, AnyxmlEffectiveStatement.class, "anyxml", "name"),
    ARGUMENT(ArgumentStatement.class, ArgumentEffectiveStatement.class, "argument", "name"),
    AUGMENT(AugmentStatement.class, AugmentEffectiveStatement.class, "augment", "target-node"),
    BASE(BaseStatement.class, BaseEffectiveStatement.class, "base", "name"),
    BELONGS_TO(BelongsToStatement.class, BelongsToEffectiveStatement.class, "belongs-to", "module"),
    BIT(BitStatement.class, BitEffectiveStatement.class, "bit", "name"),
    CASE(CaseStatement.class, CaseEffectiveStatement.class, "case", "name"),
    CHOICE(ChoiceStatement.class, ChoiceEffectiveStatement.class, "choice", "name"),
    CONFIG(ConfigStatement.class, ConfigEffectiveStatement.class, "config", "value"),
    CONTACT(ContactStatement.class, ContactEffectiveStatement.class, "contact", "text", true),
    CONTAINER(ContainerStatement.class, ContainerEffectiveStatement.class, "container", "name"),
    DEFAULT(DefaultStatement.class, DefaultEffectiveStatement.class, "default", "value"),
    DESCRIPTION(DescriptionStatement.class, DescriptionEffectiveStatement.class, "description", "text", true),
    DEVIATE(DeviateStatement.class, DeviateEffectiveStatement.class, "deviate", "value"),
    DEVIATION(DeviationStatement.class, DeviationEffectiveStatement.class, "deviation", "target-node"),
    ENUM(EnumStatement.class, EnumEffectiveStatement.class, "enum", "name"),
    ERROR_APP_TAG(ErrorAppTagStatement.class, ErrorAppTagEffectiveStatement.class, "error-app-tag", "value"),
    ERROR_MESSAGE(ErrorMessageStatement.class, ErrorMessageEffectiveStatement.class, "error-message", "value", true),
    EXTENSION(ExtensionStatement.class, ExtensionEffectiveStatement.class, "extension", "name"),
    FEATURE(FeatureStatement.class, FeatureEffectiveStatement.class, "feature", "name"),
    FRACTION_DIGITS(FractionDigitsStatement.class, FractionDigitsEffectiveStatement.class, "fraction-digits", "value"),
    GROUPING(GroupingStatement.class, GroupingEffectiveStatement.class, "grouping", "name"),
    IDENTITY(IdentityStatement.class, IdentityEffectiveStatement.class, "identity", "name"),
    IF_FEATURE(IfFeatureStatement.class, IfFeatureEffectiveStatement.class, "if-feature", "name"),
    IMPORT(ImportStatement.class, ImportEffectiveStatement.class, "import", "module"),
    INCLUDE(IncludeStatement.class, IncludeEffectiveStatement.class, "include", "module"),
    INPUT(InputStatement.class, InputEffectiveStatement.class, "input"),
    KEY(KeyStatement.class, KeyEffectiveStatement.class, "key", "value"),
    LEAF(LeafStatement.class, LeafEffectiveStatement.class, "leaf", "name"),
    LEAF_LIST(LeafListStatement.class, LeafListEffectiveStatement.class, "leaf-list", "name"),
    LENGTH(LengthStatement.class, LengthEffectiveStatement.class, "length", "value"),
    LIST(ListStatement.class, ListEffectiveStatement.class, "list", "name"),
    MANDATORY(MandatoryStatement.class, MandatoryEffectiveStatement.class, "mandatory", "value"),
    MAX_ELEMENTS(MaxElementsStatement.class, MaxElementsEffectiveStatement.class, "max-elements", "value"),
    MIN_ELEMENTS(MinElementsStatement.class, MinElementsEffectiveStatement.class, "min-elements", "value"),
    MODIFIER(ModifierStatement.class, ModifierEffectiveStatement.class, "modifier", "value"),
    MODULE(ModuleStatement.class, ModuleEffectiveStatement.class, "module", "name"),
    MUST(MustStatement.class, MustEffectiveStatement.class, "must", "condition"),
    NAMESPACE(NamespaceStatement.class, NamespaceEffectiveStatement.class, "namespace", "uri"),
    NOTIFICATION(NotificationStatement.class, NotificationEffectiveStatement.class, "notification", "name"),
    ORDERED_BY(OrderedByStatement.class, OrderedByEffectiveStatement.class, "ordered-by", "value"),
    ORGANIZATION(OrganizationStatement.class, OrganizationEffectiveStatement.class, "organization", "text", true),
    OUTPUT(OutputStatement.class, OutputEffectiveStatement.class, "output"),
    PATH(PathStatement.class, PathEffectiveStatement.class, "path", "value"),
    PATTERN(PatternStatement.class, PatternEffectiveStatement.class, "pattern", "value"),
    POSITION(PositionStatement.class, PositionEffectiveStatement.class, "position", "value"),
    PREFIX(PrefixStatement.class, PrefixEffectiveStatement.class, "prefix", "value"),
    PRESENCE(PresenceStatement.class, PresenceEffectiveStatement.class, "presence", "value"),
    RANGE(RangeStatement.class, RangeEffectiveStatement.class, "range", "value"),
    REFERENCE(ReferenceStatement.class, ReferenceEffectiveStatement.class, "reference", "text", true),
    REFINE(RefineStatement.class, RefineEffectiveStatement.class, "refine", "target-node"),
    REQUIRE_INSTANCE(RequireInstanceStatement.class, RequireInstanceEffectiveStatement.class, "require-instance",
        "value"),
    REVISION(RevisionStatement.class, RevisionEffectiveStatement.class, "revision", "date"),
    REVISION_DATE(RevisionDateStatement.class, RevisionDateEffectiveStatement.class, "revision-date", "date"),
    RPC(RpcStatement.class, RpcEffectiveStatement.class, "rpc", "name"),
    STATUS(StatusStatement.class, StatusEffectiveStatement.class, "status", "value"),
    SUBMODULE(SubmoduleStatement.class, SubmoduleEffectiveStatement.class, "submodule", "name"),
    @SuppressWarnings({ "unchecked", "rawtypes" })
    TYPE(TypeStatement.class, (Class) TypeEffectiveStatement.class, "type", "name"),
    TYPEDEF(TypedefStatement.class, TypedefEffectiveStatement.class, "typedef", "name"),
    UNIQUE(UniqueStatement.class, UniqueEffectiveStatement.class, "unique", "tag"),
    UNITS(UnitsStatement.class, UnitsEffectiveStatement.class, "units", "name"),
    USES(UsesStatement.class, UsesEffectiveStatement.class, "uses", "name"),
    VALUE(ValueStatement.class, ValueEffectiveStatement.class, "value", "value"),
    WHEN(WhenStatement.class, WhenEffectiveStatement.class, "when", "condition"),
    YANG_VERSION(YangVersionStatement.class, YangVersionEffectiveStatement.class, "yang-version", "value"),
    YIN_ELEMENT(YinElementStatement.class, YinElementEffectiveStatement.class, "yin-element", "value");

    private final Class<? extends DeclaredStatement<?>> declaredType;
    private final Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final QName name;
    private final @Nullable QName argument;
    private final boolean yinElement;

    @SuppressFBWarnings("NP_STORE_INTO_NONNULL_FIELD")
    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr) {
        declaredType = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = yinQName(nameStr);
        argument = null;
        yinElement = false;
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr, final String argumentStr) {
        this(declared, effective, nameStr, argumentStr, false);
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String nameStr, final String argumentStr,
            final boolean yinElement) {
        declaredType = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = yinQName(nameStr);
        argument = yinQName(argumentStr);
        this.yinElement = yinElement;
    }

    private static QName yinQName(final String nameStr) {
        return QName.create(YangConstants.RFC6020_YIN_MODULE, nameStr).intern();
    }

    @Override
    public QName getStatementName() {
        return name;
    }

    @Override
    public Optional<ArgumentDefinition> getArgumentDefinition() {
        return ArgumentDefinition.ofNullable(argument, yinElement);
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredType;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }
}
