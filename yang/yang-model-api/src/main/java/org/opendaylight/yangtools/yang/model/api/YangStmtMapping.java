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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;

/**
 * Mapping for both RFC6020 and RFC7950 statements.
 */
@Beta
public enum YangStmtMapping implements StatementDefinition {
    ACTION(ActionStatement.class, "action", "name"),
    ANYDATA(AnydataStatement.class, "anydata", "name"),
    ANYXML(AnyxmlStatement.class, "anyxml", "name"),
    ARGUMENT(ArgumentStatement.class, "argument", "name"),
    AUGMENT(AugmentStatement.class, "augment", "target-node"),
    BASE(BaseStatement.class, "base", "name"),
    BELONGS_TO(BelongsToStatement.class, "belongs-to", "module"),
    BIT(BitStatement.class, "bit", "name"),
    CASE(CaseStatement.class, "case", "name"),
    CHOICE(ChoiceStatement.class, "choice", "name"),
    CONFIG(ConfigStatement.class, "config", "value"),
    CONTACT(ContactStatement.class, "contact", "text", true),
    CONTAINER(ContainerStatement.class, "container", "name"),
    DEFAULT(DefaultStatement.class, "default", "value"),
    DESCRIPTION(DescriptionStatement.class, "description", "text", true),
    DEVIATE(DeviateStatement.class, "deviate", "value"),
    DEVIATION(DeviationStatement.class, "deviation", "target-node"),
    ENUM(EnumStatement.class, "enum", "name"),
    ERROR_APP_TAG(ErrorAppTagStatement.class, "error-app-tag", "value"),
    ERROR_MESSAGE(ErrorMessageStatement.class, "error-message", "value", true),
    EXTENSION(ExtensionStatement.class, "extension", "name"),
    FEATURE(FeatureStatement.class, "feature", "name"),
    FRACTION_DIGITS(FractionDigitsStatement.class, "fraction-digits", "value"),
    GROUPING(GroupingStatement.class, "grouping", "name"),
    IDENTITY(IdentityStatement.class, "identity", "name"),
    IF_FEATURE(IfFeatureStatement.class, "if-feature", "name"),
    IMPORT(ImportStatement.class, "import", "module"),
    INCLUDE(IncludeStatement.class, "include", "module"),
    INPUT(InputStatement.class, "input"),
    KEY(KeyStatement.class, "key", "value"),
    LEAF(LeafStatement.class, "leaf", "name"),
    LEAF_LIST(LeafListStatement.class, "leaf-list", "name"),
    LENGTH(LengthStatement.class, "length", "value"),
    LIST(ListStatement.class, "list", "name"),
    MANDATORY(MandatoryStatement.class, "mandatory", "value"),
    MAX_ELEMENTS(MaxElementsStatement.class, "max-elements", "value"),
    MIN_ELEMENTS(MinElementsStatement.class, "min-elements", "value"),
    MODIFIER(ModifierStatement.class, "modifier", "value"),
    MODULE(ModuleStatement.class, "module", "name"),
    MUST(MustStatement.class, "must", "condition"),
    NAMESPACE(NamespaceStatement.class, "namespace", "uri"),
    NOTIFICATION(NotificationStatement.class, "notification", "name"),
    ORDERED_BY(OrderedByStatement.class, "ordered-by", "value"),
    ORGANIZATION(OrganizationStatement.class, "organization", "text", true),
    OUTPUT(OutputStatement.class, "output"),
    PATH(PathStatement.class, "path", "value"),
    PATTERN(PatternStatement.class, "pattern", "value"),
    POSITION(PositionStatement.class, "position", "value"),
    PREFIX(PrefixStatement.class, "prefix", "value"),
    PRESENCE(PresenceStatement.class, "presence", "value"),
    RANGE(RangeStatement.class, "range", "value"),
    REFERENCE(ReferenceStatement.class, "reference", "text", true),
    REFINE(RefineStatement.class, "refine", "target-node"),
    REQUIRE_INSTANCE(RequireInstanceStatement.class, "require-instance", "value"),
    REVISION(RevisionStatement.class, "revision", "date"),
    REVISION_DATE(RevisionDateStatement.class, "revision-date", "date"),
    RPC(RpcStatement.class, "rpc", "name"),
    STATUS(StatusStatement.class, "status", "value"),
    SUBMODULE(SubmoduleStatement.class, "submodule", "name"),
    TYPE(TypeStatement.class, "type", "name"),
    TYPEDEF(TypedefStatement.class, "typedef", "name"),
    UNIQUE(UniqueStatement.class, "unique", "tag"),
    UNITS(UnitsStatement.class, "units", "name"),
    USES(UsesStatement.class, "uses", "name"),
    VALUE(ValueStatement.class, "value", "value"),
    WHEN(WhenStatement.class, "when", "condition"),
    YANG_VERSION(YangVersionStatement.class, "yang-version", "value"),
    YIN_ELEMENT(YinElementStatement.class, "yin-element", "value");

    private final Class<? extends DeclaredStatement<?>> type;
    private final Class<? extends EffectiveStatement<?,?>> effectiveType;
    private final QName name;
    private final QName argument;
    private final boolean yinElement;

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> clz, final String nameStr) {
        type = requireNonNull(clz);
        //FIXME: fill up effective type correctly
        effectiveType = null;
        name = yinQName(nameStr);
        argument = null;
        yinElement = false;
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> clz, final String nameStr, final String argumentStr) {
        type = requireNonNull(clz);
        //FIXME: fill up effective type correctly
        effectiveType = null;
        name = yinQName(nameStr);
        argument = yinQName(argumentStr);
        this.yinElement = false;
    }

    YangStmtMapping(final Class<? extends DeclaredStatement<?>> clz, final String nameStr, final String argumentStr,
            final boolean yinElement) {
        type = requireNonNull(clz);
        //FIXME: fill up effective type correctly
        effectiveType = null;
        name = yinQName(nameStr);
        argument = yinQName(argumentStr);
        this.yinElement = yinElement;
    }

    @Nonnull private static QName yinQName(final String nameStr) {
        return QName.create(YangConstants.RFC6020_YIN_MODULE, nameStr).intern();
    }

    @Nonnull
    @Override
    public QName getStatementName() {
        return name;
    }

    @Override
    @Nullable public QName getArgumentName() {
        return argument;
    }

    @Override
    @Nonnull public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }

    @Nonnull
    @Override
    public Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }
}

