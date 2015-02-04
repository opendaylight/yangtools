/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafref.parser;

import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.Module;
import java.util.LinkedList;
import java.util.List;
import java.net.URI;
import org.opendaylight.yangtools.leafrefcontext.QNameWithPredicate;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Absolute_pathContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Absolute_schema_nodeidContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Current_function_invocationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Descendant_pathContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Descendant_schema_nodeidContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.IdentifierContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Node_identifierContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_equality_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_key_exprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_predicateContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.PrefixContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Rel_path_keyexprContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Relative_pathContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParserListener;



public final class LeafRefPathParserListenerImpl implements LeafRefPathParserListener{

    private SchemaContext schemaContext;
    private Module module;
    private SchemaPath leafRefPath;
    private boolean absolute;
    private QNameWithPredicate currentQNameWithPredicate;
    private URI currentQNameNamespace;
    private String currentQNameLocalName;
    private LinkedList<QName> qnameList;
    SchemaNode node;

    public LeafRefPathParserListenerImpl(SchemaContext schemaContext, Module currentModule, SchemaNode currentNode) {
       this.schemaContext = schemaContext;
       this.module = currentModule;
       this.qnameList = new LinkedList<QName>();
       this.node=currentNode;
    }


    @Override
    public void visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visitErrorNode(ErrorNode node) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterCurrent_function_invocation(
            Current_function_invocationContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitCurrent_function_invocation(
            Current_function_invocationContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterDescendant_path(Descendant_pathContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitDescendant_path(Descendant_pathContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterPath_predicate(Path_predicateContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitPath_predicate(Path_predicateContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterRel_path_keyexpr(Rel_path_keyexprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitRel_path_keyexpr(Rel_path_keyexprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterDescendant_schema_nodeid(
            Descendant_schema_nodeidContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitDescendant_schema_nodeid(Descendant_schema_nodeidContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterRelative_path(Relative_pathContext ctx) {
        absolute = false;
    }


    @Override
    public void exitRelative_path(Relative_pathContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterPath_equality_expr(Path_equality_exprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitPath_equality_expr(Path_equality_exprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterAbsolute_schema_nodeid(Absolute_schema_nodeidContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitAbsolute_schema_nodeid(Absolute_schema_nodeidContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterPrefix(PrefixContext ctx) {
        currentQNameNamespace = getNamespaceForImportPrefix(ctx.getText());
    }

    private URI getNamespaceForImportPrefix(String prefix){
        ModuleImport moduleImport = getModuleImport(prefix);
        Module findedModule = schemaContext.findModuleByName(moduleImport.getModuleName(), moduleImport.getRevision());

        return findedModule.getNamespace();
    }


    private ModuleImport getModuleImport(String prefix) {
        Set<ModuleImport> imports = module.getImports();

        for (ModuleImport moduleImport : imports) {
            if(moduleImport.getPrefix().equals(prefix)) {
                return moduleImport;
            }
        }
        return null;
    }


    @Override
    public void exitPrefix(PrefixContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterPath_key_expr(Path_key_exprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitPath_key_expr(Path_key_exprContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterAbsolute_path(Absolute_pathContext ctx) {
        absolute = true;
    }


    @Override
    public void exitAbsolute_path(Absolute_pathContext ctx) {

    }


    @Override
    public void enterPath_arg(Path_argContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void exitPath_arg(Path_argContext ctx) {
        leafRefPath = SchemaPath.create(qnameList,absolute);
    }


    @Override
    public void enterIdentifier(IdentifierContext ctx) {
        currentQNameLocalName = ctx.getText();
    }


    @Override
    public void exitIdentifier(IdentifierContext ctx) {

    }


    @Override
    public void enterNode_identifier(Node_identifierContext ctx) {


    }


    @Override
    public void exitNode_identifier(Node_identifierContext ctx) {

        if (currentQNameNamespace == null)
            currentQNameNamespace = module.getNamespace();

        QNameWithPredicate qname = new QNameWithPredicate(
                currentQNameNamespace, currentQNameLocalName);

        currentQNameNamespace = null;
        currentQNameLocalName = null;
        currentQNameWithPredicate = qname;
        qnameList.add(qname);

    }

    public SchemaPath getLeafRefPath() {
        return leafRefPath;
    }

}
