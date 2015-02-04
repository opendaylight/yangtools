/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafref.parser;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import org.opendaylight.yangtools.leafrefcontext.builder.QNamePredicateBuilder;
import org.opendaylight.yangtools.leafrefcontext.builder.QNameWithPredicateBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;



public final class LeafRefPathParserListenerImpl implements LeafRefPathParserListener{

    private SchemaContext schemaContext;
    private Module module;
    private LeafRefPath leafRefPath;
    private boolean relativePath=false;
    private QNameWithPredicateBuilder currentLeafRefPathQName;
    private QNamePredicateBuilder currentPredicate;
    private QNameModule currentQnameModule;
    private String currentQNameLocalName;
    private LinkedList<QNameWithPredicateBuilder> leafRefPathQnameList;
    private LinkedList<QNameWithPredicateBuilder> predicatePathKeyQnameList;
    private SchemaNode node; //FIXME use for identifier path completion
    private ParsingState currentParsingState;

    Function<QNameWithPredicateBuilder, QNameWithPredicate> build = new Function<QNameWithPredicateBuilder, QNameWithPredicate>() {
        @Override
        public QNameWithPredicate apply(QNameWithPredicateBuilder builder) {
           return builder.build();
        }
     };

    private enum ParsingState {
        LEAF_REF_PATH, PATH_PREDICATE, PREDICATE_PATH_EQUALITY_EXPR, PATH_KEY_EXPR
    }


    public LeafRefPathParserListenerImpl(SchemaContext schemaContext, Module currentModule, SchemaNode currentNode) {
       this.schemaContext = schemaContext;
       this.module = currentModule;
       this.leafRefPathQnameList = new LinkedList<QNameWithPredicateBuilder>();
       this.node=currentNode;
       this.currentParsingState = ParsingState.LEAF_REF_PATH;
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
        currentParsingState=ParsingState.PATH_PREDICATE;
        currentPredicate = new QNamePredicateBuilder();
    }


    @Override
    public void exitPath_predicate(Path_predicateContext ctx) {

        currentLeafRefPathQName.addQNamePredicate(currentPredicate.build());
        currentPredicate = null;

        currentParsingState=ParsingState.LEAF_REF_PATH;
    }


    @Override
    public void enterRel_path_keyexpr(Rel_path_keyexprContext ctx) {
        currentParsingState=ParsingState.PATH_KEY_EXPR;

        predicatePathKeyQnameList = new LinkedList<QNameWithPredicateBuilder>();
        List<TerminalNode> dots = ctx.DOTS();
        for (TerminalNode parent : dots) {
            predicatePathKeyQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }
    }


    @Override
    public void exitRel_path_keyexpr(Rel_path_keyexprContext ctx) {

        LeafRefPath pathKeyExpression = LeafRefPath.create(Lists.transform(predicatePathKeyQnameList,build), false);
        currentPredicate.setPathKeyExpression(pathKeyExpression);

        currentParsingState=ParsingState.PREDICATE_PATH_EQUALITY_EXPR;
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

        relativePath = true;
        List<TerminalNode> dots = ctx.DOTS();
        for (TerminalNode parent : dots) {
            leafRefPathQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }

    }


    @Override
    public void exitRelative_path(Relative_pathContext ctx) {
        // TODO Auto-generated method stub

    }


    @Override
    public void enterPath_equality_expr(Path_equality_exprContext ctx) {
        currentParsingState=ParsingState.PREDICATE_PATH_EQUALITY_EXPR;


    }


    @Override
    public void exitPath_equality_expr(Path_equality_exprContext ctx) {

        currentParsingState=ParsingState.PATH_PREDICATE;
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

        if (module.getPrefix().equals(ctx.getText())) {
            currentQnameModule = module.getQNameModule();
        } else {
            currentQnameModule = getQNameModuleForImportPrefix(ctx.getText());
        }
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
        leafRefPath = LeafRefPath.create(Lists.transform(leafRefPathQnameList,build), !relativePath);
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

        if (currentQnameModule == null) {
            currentQnameModule = module.getQNameModule();
        }

        if (currentParsingState == ParsingState.PREDICATE_PATH_EQUALITY_EXPR) {
            QName qname = QName.create(currentQnameModule,
                    currentQNameLocalName);
            currentPredicate.setIdentifier(qname);
        } else {

            QNameWithPredicateBuilder qnameBuilder = new QNameWithPredicateBuilder(
                    currentQnameModule, currentQNameLocalName);

            if (currentParsingState == ParsingState.PATH_KEY_EXPR) {
                predicatePathKeyQnameList.add(qnameBuilder);
            } else if (currentParsingState == ParsingState.LEAF_REF_PATH) {
                currentLeafRefPathQName = qnameBuilder;
                leafRefPathQnameList.add(qnameBuilder);
            }
        }
        currentQnameModule = null;
        currentQNameLocalName = null;
    }

    public LeafRefPath getLeafRefPath() {
        return leafRefPath;
    }


    private URI getNamespaceForImportPrefix(String prefix){
        ModuleImport moduleImport = getModuleImport(prefix);
        Module findedModule = schemaContext.findModuleByName(moduleImport.getModuleName(), moduleImport.getRevision());

        return findedModule.getNamespace();
    }

    private QNameModule getQNameModuleForImportPrefix(String prefix) {
        ModuleImport moduleImport = getModuleImport(prefix);

        if (moduleImport == null) {
            throw new LeafRefPathParseException("No module import for prefix: "
                    + prefix + " in module: " + module.getName());
        }

        String moduleName = moduleImport.getModuleName();
        Date revision = moduleImport.getRevision();
        Module findedModule = schemaContext.findModuleByName(moduleName,
                revision);

        return findedModule.getQNameModule();
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

}
