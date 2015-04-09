/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

@Beta
public enum Rfc6020Mapping implements StatementDefinition {
    Anyxml(AnyxmlStatement.class, "anyxml", "name"),
    Argument(ArgumentStatement.class, "argument", "name"),
    Augment(AugmentStatement.class, "augment", "target-node"),
    Base(BaseStatement.class, "base", "name"),
    BelongsTo(BelongsToStatement.class, "belongs-to", "module"),
    Bit(BitStatement.class, "bit", "name"),
    Case(CaseStatement.class, "case", "name"),
    Choice(ChoiceStatement.class, "choice", "name"),
    Config(ConfigStatement.class, "config", "value"),
    Contact(ContactStatement.class, "contact", "text", true),
    Container(ContainerStatement.class, "container", "name"),
    Default(DefaultStatement.class, "default", "value"),
    Description(DescriptionStatement.class, "description", "text", true),
    Deviate(DeviateStatement.class, "deviate", "value"),
    Deviation(DeviationStatement.class, "deviation", "target-node"),
    Enum(EnumStatement.class, "enum", "name"),
    ErrorAppTag(ErrorAppTagStatement.class, "error-app-tag", "value"),
    ErrorMessage(ErrorMessageStatement.class, "error-message", "value", true),
    Extension(ExtensionStatement.class, "extension", "name"),
    Feature(FeatureStatement.class, "feature", "name"),
    FractionDigits(FractionDigitsStatement.class, "fraction-digits", "value"),
    Grouping(GroupingStatement.class, "grouping", "name"),
    Identity(IdentityStatement.class, "identity", "name"),
    IfFeature(IfFeatureStatement.class, "if-feature", "name"),
    Import(ImportStatement.class, "import", "module"),
    Include(IncludeStatement.class, "include", "module"),
    Input(InputStatement.class, "input"),
    Key(KeyStatement.class, "key", "value"),
    Leaf(LeafStatement.class, "leaf", "name"),
    LeafList(LeafListStatement.class, "leaf-list", "name"),
    Length(LengthStatement.class, "length", "value"),
    List(ListStatement.class, "list", "name"),
    Mandatory(MandatoryStatement.class, "mandatory", "value"),
    MaxElements(MaxElementsStatement.class, "max-elements", "value"),
    MinElements(MinElementsStatement.class, "min-elements", "value"),
    Module(ModuleStatement.class, "module", "name"),
    Must(MustStatement.class, "must", "condition"),
    Namespace(NamespaceStatement.class, "namespace", "uri"),
    Notification(NotificationStatement.class, "notification", "name"),
    OrderedBy(OrderedByStatement.class, "ordered-by", "value"),
    Organization(OrganizationStatement.class, "organization", "text", true),
    Output(OutputStatement.class, "output"),
    Path(PathStatement.class, "path", "value"),
    Pattern(PatternStatement.class, "pattern", "value"),
    Position(PositionStatement.class, "position", "value"),
    Prefix(PrefixStatement.class, "prefix", "value"),
    Presence(PresenceStatement.class, "presence", "value"),
    Range(RangeStatement.class, "range", "value"),
    Reference(ReferenceStatement.class, "reference", "text", true),
    Refine(RefineStatement.class, "refine", "target-node"),
    RequireInstance(RequireInstanceStatement.class, "require-instance", "value"),
    Revision(RevisionStatement.class, "revision", "date"),
    RevisionDate(RevisionDateStatement.class, "revision-date", "date"),
    Rpc(RpcStatement.class, "rpc", "name"),
    Status(StatusStatement.class, "status", "value"),
    Submodule(SubmoduleStatement.class, "submodule", "name"),
    Type(TypeStatement.class, "type", "name"),
    Typedef(TypedefStatement.class, "typedef", "name"),
    Unique(UniqueStatement.class, "unique", "tag"),
    Units(UnitsStatement.class, "units", "name"),
    Uses(UsesStatement.class, "uses", "name"),
    Value(ValueStatement.class, "value", "value"),
    When(WhenStatement.class, "when", "condition"),
    YangVersion(YangVersionStatement.class, "yang-version", "value"),
    YinElement(YinElementStatement.class, "yin-element", "value");

//    StringRestrictions(TypeStatement.StringRestrictions.class, "string-restrictions", "type"),

    // solved
//    EnumSpecification(TypeStatement.EnumSpecification.class, "enum", "type");
//    Decimal64(TypeStatement.Decimal64Specification.class, "range", "type"),
//    IdentityRef(TypeStatement.IdentityRefSpecification.class, "identityref", "type"),
//    InstanceIdentifier(TypeStatement.InstanceIdentifierSpecification.class, "instance-identifier", "type"),
//    LeafRef(TypeStatement.LeafrefSpecification.class, "leaf-ref", "type"),
//    NumericalRestrictions(TypeStatement.NumericalRestrictions.class, "numerical-restrictions", "type"),
//    Union(TypeStatement.UnionSpecification.class, "union", "type"),
//    Bits(TypeStatement.BitsSpecification.class, "bits", "type");

    private final
    @Nonnull
    Class<? extends DeclaredStatement<?>> type;
    private final
    @Nonnull
    QName name;
    private final
    @Nullable
    QName argument;
    private final boolean yinElement;


    private Rfc6020Mapping(Class<? extends DeclaredStatement<?>> clz, final String nameStr) {
        type = Preconditions.checkNotNull(clz);
        name = yinQName(nameStr);
        argument = null;
        yinElement = false;
    }

    private Rfc6020Mapping(Class<? extends DeclaredStatement<?>> clz, final String nameStr, final String argumentStr) {
        type = Preconditions.checkNotNull(clz);
        name = yinQName(nameStr);
        argument = yinQName(argumentStr);
        this.yinElement = false;
    }

    private Rfc6020Mapping(Class<? extends DeclaredStatement<?>> clz, final String nameStr, final String argumentStr,
                           final boolean yinElement) {
        type = Preconditions.checkNotNull(clz);
        name = yinQName(nameStr);
        argument = yinQName(argumentStr);
        this.yinElement = yinElement;
    }

    private static QName yinQName(String nameStr) {
        return QName.cachedReference(QName.create(YangConstants.RFC6020_YIN_MODULE, nameStr));
    }

    @Override
    public QName getStatementName() {
        return name;
    }

    @Override
    public QName getArgumentName() {
        return argument;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getEffectiveRepresentationClass() {
        // FIXME: Add support once these interfaces are defined.
        throw new UnsupportedOperationException("Not defined yet.");
    }

    public boolean isArgumentYinElement() {
        return yinElement;
    }
}

