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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
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
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
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
// FIXME: eliminate this class
@Beta
public enum YangStmtMapping implements StatementDefinition {
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
    CONTAINER(ContainerStatement.class, ContainerEffectiveStatement.class, "container", "name"),
    DEFAULT(DefaultStatement.class, DefaultEffectiveStatement.class, "default", "value"),
    DEVIATE(DeviateStatement.class, DeviateEffectiveStatement.class, "deviate", "value"),
    DEVIATION(DeviationStatement.class, DeviationEffectiveStatement.class, "deviation", "target-node"),
    ENUM(EnumStatement.class, EnumEffectiveStatement.class, "enum", "name"),
    ERROR_APP_TAG(ErrorAppTagStatement.class, ErrorAppTagEffectiveStatement.class, "error-app-tag", "value"),
    EXTENSION(ExtensionStatement.class, ExtensionEffectiveStatement.class, "extension", "name"),
    FEATURE(FeatureStatement.class, FeatureEffectiveStatement.class, "feature", "name"),
    FRACTION_DIGITS(FractionDigitsStatement.class, FractionDigitsEffectiveStatement.class, "fraction-digits", "value"),
    GROUPING(GroupingStatement.class, GroupingEffectiveStatement.class, "grouping", "name"),
    IDENTITY(IdentityStatement.class, IdentityEffectiveStatement.class, "identity", "name"),
    IF_FEATURE(IfFeatureStatement.class, IfFeatureEffectiveStatement.class, "if-feature", "name"),
    IMPORT(ImportStatement.class, ImportEffectiveStatement.class, "import", "module"),
    INCLUDE(IncludeStatement.class, IncludeEffectiveStatement.class, "include", "module"),
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
    PATH(PathStatement.class, PathEffectiveStatement.class, "path", "value"),
    PATTERN(PatternStatement.class, PatternEffectiveStatement.class, "pattern", "value"),
    POSITION(PositionStatement.class, PositionEffectiveStatement.class, "position", "value"),
    PREFIX(PrefixStatement.class, PrefixEffectiveStatement.class, "prefix", "value"),
    PRESENCE(PresenceStatement.class, PresenceEffectiveStatement.class, "presence", "value"),
    RANGE(RangeStatement.class, RangeEffectiveStatement.class, "range", "value"),
    REFINE(RefineStatement.class, RefineEffectiveStatement.class, "refine", "target-node"),
    REQUIRE_INSTANCE(RequireInstanceStatement.class, RequireInstanceEffectiveStatement.class, "require-instance",
        "value"),
    REVISION(RevisionStatement.class, RevisionEffectiveStatement.class, "revision", "date"),
    REVISION_DATE(RevisionDateStatement.class, RevisionDateEffectiveStatement.class, "revision-date", "date"),
    SUBMODULE(SubmoduleStatement.class, SubmoduleEffectiveStatement.class, "submodule", "name"),
    @SuppressWarnings({ "unchecked", "rawtypes" })
    TYPE(TypeStatement.class, (Class) TypeEffectiveStatement.class, "type", "name"),
    TYPEDEF(TypedefStatement.class, TypedefEffectiveStatement.class, "typedef", "name"),
    UNIQUE(UniqueStatement.class, UniqueEffectiveStatement.class, "unique", "tag"),
    USES(UsesStatement.class, UsesEffectiveStatement.class, "uses", "name"),
    VALUE(ValueStatement.class, ValueEffectiveStatement.class, "value", "value"),
    WHEN(WhenStatement.class, WhenEffectiveStatement.class, "when", "condition"),
    YANG_VERSION(YangVersionStatement.class, YangVersionEffectiveStatement.class, "yang-version", "value"),
    YIN_ELEMENT(YinElementStatement.class, YinElementEffectiveStatement.class, "yin-element", "value");

    private final @NonNull Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final @NonNull Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final @NonNull QName statementName;
    private final @Nullable ArgumentDefinition argumentDefinition;

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String name, final String argName) {
        this(declared, effective, name, argName, false);
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> declared,
            final Class<? extends EffectiveStatement<?, ?>> effective, final String name, final String argName,
            final boolean yinElement) {
        declaredRepresentation = requireNonNull(declared);
        effectiveRepresentation = requireNonNull(effective);
        statementName = qualifyName(name);
        argumentDefinition = new ArgumentDefinition(qualifyName(argName), yinElement);
    }

    private static @NonNull QName qualifyName(final String name) {
        return QName.create(YangConstants.RFC6020_YIN_MODULE, name).intern();
    }

    @Override
    public QName statementName() {
        return statementName;
    }

    @Override
    public ArgumentDefinition argumentDefinition() {
        return argumentDefinition;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> declaredRepresentation() {
        return declaredRepresentation;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation() {
        return effectiveRepresentation;
    }

    @Override
    public String toString() {
        return StatementDefinition.toString(this);
    }
}
