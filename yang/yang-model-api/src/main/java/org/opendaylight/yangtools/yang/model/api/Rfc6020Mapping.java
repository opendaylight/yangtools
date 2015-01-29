/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

// FIXME: Consider moving to yang-schema or yang-model-util
@Beta
public enum Rfc6020Mapping implements StatementDefinition {
    Anyxml(AnyxmlStatement.class,"anyxml","name",false),
    Argument(ArgumentStatement.class,"argument","name",false),
    Augment(AugmentStatement.class,"augment","target-node",false),
    Base(BaseStatement.class,"base","name",false),
    BelongsTo(BelongsToStatement.class,"belongs-to","module",false),
    Bit(BitStatement.class,"bit","name",false),
    Case(CaseStatement.class, "case","name",false),
    Choice(ChoiceStatement.class, "choice","name",false),
    Config(ConfigStatement.class,"config","value",false),
    Contact(ContactStatement.class,"contact","text",true),
    Container(ContainerStatement.class,"container","name",false),
    Default(DefaultStatement.class,"default","value",false),
    Description(DescriptionStatement.class,"description","text",true),
    Deviate(DeviateStatement.class,"deviate","value",false),
    Deviation(DeviationStatement.class,"deviation","target-node",false),
    Enum(EnumStatement.class,"enum","name",false),
    ErrorAppTag(ErrorAppTagStatement.class,"error-app-tag","value",false),
    ErrorMessage(ErrorMessageStatement.class,"error-message","value",true),
    Extension(ExtensionStatement.class,"extension","name",false),
    Feature(FeatureStatement.class,"feature","name",false),
    FractionDigits(FractionDigitsStatement.class,"fraction-digits","value",false),
    Grouping(GroupingStatement.class,"grouping","name",false),
    Identity(IdentityStatement.class,"identity","name",false),
    IfFeature(IfFeatureStatement.class,"if-feature","name",false),
    Import(ImportStatement.class,"import","module",false),
    Include(IncludeStatement.class,"include","module",false),
    Input(InputStatement.class,"input",null,false),
    Key(KeyStatement.class,"key","value",false),
    Leaf(LeafStatement.class,"leaf","name",false),
    LeafList(LeafListStatement.class,"leaf-list","name",false),
    Length(LengthStatement.class,"length","value",false),
    List(ListStatement.class,"list","name",false),
    Mandatory(MandatoryStatement.class,"mandatory","value",false),
    MaxElements(MaxElementsStatement.class,"max-elements","value",false),
    MinElements(MinElementsStatement.class,"min-elements","value",false),
    Module(ModuleStatement.class,"module","name",false),
    Must(MustStatement.class,"must","condition",false),
    Namespace(NamespaceStatement.class,"namespace","uri",false),
    Notification(NotificationStatement.class,"notification","name",false),
    OrderedBy(OrderedByStatement.class,"ordered-by","value",false),
    Organization(OrganizationStatement.class,"organization","text",true),
    Output(OutputStatement.class,"output",null,false),
    Path(PathStatement.class,"path","value",false),
    Pattern(PatternStatement.class,"pattern","value",false),
    Position(PositionStatement.class,"position","value",false),
    Prefix(PrefixStatement.class,"prefix","value",false),
    Presence(PresenceStatement.class,"presence","value",false),
    Range(RangeStatement.class,"range","value",false),
    Reference(ReferenceStatement.class,"reference","text",true),
    Refine(RefineStatement.class,"refine","target-node",false),
    RequireInstance(RequireInstanceStatement.class,"require-instance","value",false),
    Revision(RevisionStatement.class,"revision","date",false),
    RevisionDate(RevisionDateStatement.class,"revision-date","date",false),
    Rpc(RpcStatement.class,"rpc","name",false),
    Status(StatusStatement.class,"status","value",false),
    Submodule(SubmoduleStatement.class,"submodule","name",false),
    Type(TypeStatement.class,"type","name",false),
    Typedef(TypedefStatement.class,"typedef","name",false),
    Unique(UniqueStatement.class,"unique","tag",false),
    Units(UnitsStatement.class,"units","name",false),
    Uses(UsesStatement.class,"uses","name",false),
    Value(ValueStatement.class,"value","value",false),
    When(WhenStatement.class,"when","condition",false),
    YangVersion(YangVersionStatement.class,"yang-version","value",false),
    YinElement(YinElementStatement.class,"yin-element","value",false);


    private final @Nonnull Class<? extends DeclaredStatement<?>> type;
    private final @Nonnull QName name;
    private final @Nullable QName argument;
    private final boolean yinElement;


    private Rfc6020Mapping(Class<? extends DeclaredStatement<?>> clz,final String nameStr,final String argumentStr,final boolean yinElement) {
        type = Preconditions.checkNotNull(clz);
        name = QName.create(YangConstants.YIN_MODULE, nameStr);
        if(argumentStr != null) {
            this.argument = QName.create(YangConstants.YIN_MODULE,argumentStr);
        } else {
            this.argument = null;
        }
        this.yinElement = yinElement;
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
        throw new UnsupportedOperationException("Not defined yet.");
    }

    public boolean isArgumentYinElement() {
        return yinElement;
    }
}

