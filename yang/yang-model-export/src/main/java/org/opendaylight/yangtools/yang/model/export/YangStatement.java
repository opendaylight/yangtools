/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;

// FIXME: Consider moving to yang-schema or yang-model-util
@Beta
public enum YangStatement implements StatementDefinition {
    Anyxml("anyxml","name",false),
    Argument("argument","name",false),
    Augment("augment","target-node",false),
    Base("base","name",false),
    BelongsTo("belongs-to","module",false),
    Bit("bit","name",false),
    Case("case","name",false),
    Choice("choice","name",false),
    Config("config","value",false),
    Contact("contact","text",true),
    Container("container","name",false),
    Default("default","value",false),
    Description("description","text",true),
    Deviate("deviate","value",false),
    Deviation("deviation","target-node",false),
    Enum("enum","name",false),
    ErrorAppTag("error-app-tag","value",false),
    ErrorMessage("error-message","value",true),
    Extension("extension","name",false),
    Feature("feature","name",false),
    FractionDigits("fraction-digits","value",false),
    Grouping("grouping","name",false),
    Identity("identity","name",false),
    IfFeature("if-feature","name",false),
    Import("import","module",false),
    Include("include","module",false),
    Input("input",null,false),
    Key("key","value",false),
    Leaf("leaf","name",false),
    LeafList("leaf-list","name",false),
    Length("length","value",false),
    List("list","name",false),
    Mandatory("mandatory","value",false),
    MaxElements("max-elements","value",false),
    MinElements("min-elements","value",false),
    Module("module","name",false),
    Must("must","condition",false),
    Namespace("namespace","uri",false),
    Notification("notification","name",false),
    OrderedBy("ordered-by","value",false),
    Organization("organization","text",true),
    Output("output",null,false),
    Path("path","value",false),
    Pattern("pattern","value",false),
    Position("position","value",false),
    Prefix("prefix","value",false),
    Presence("presence","value",false),
    Range("range","value",false),
    Reference("reference","text",true),
    Refine("refine","target-node",false),
    RequireInstance("require-instance","value",false),
    Revision("revision","date",false),
    RevisionDate("revision-date","date",false),
    Rpc("rpc","name",false),
    Status("status","value",false),
    Submodule("submodule","name",false),
    Type("type","name",false),
    Typedef("typedef","name",false),
    Unique("unique","tag",false),
    Units("units","name",false),
    Uses("uses","name",false),
    Value("value","value",false),
    When("when","condition",false),
    YangVersion("yang-version","value",false),
    YinElement("yin-element","value",false);


    private final QName name;
    private final QName argument;
    private final boolean yinElement;


    private YangStatement(final String nameStr,final String argumentStr,final boolean yinElement) {
        // TODO Auto-generated constructor stub
        name = QName.create(YangConstants.YIN_MODULE, nameStr);
        if(argumentStr != null) {
            this.argument = QName.create(YangConstants.YIN_MODULE,argumentStr);
        } else {
            this.argument = null;
        }
        this.yinElement = yinElement;
    }

    @Override
    public QName getIdentifier() {
        return name;
    }

    @Override
    public QName getArgumentName() {
        return argument;
    }

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }
}
