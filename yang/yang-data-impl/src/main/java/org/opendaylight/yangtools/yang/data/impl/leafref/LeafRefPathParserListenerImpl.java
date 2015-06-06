/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.IdentifierContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Node_identifierContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Path_argContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Path_equality_exprContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Path_predicateContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.PrefixContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Rel_path_keyexprContext;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Relative_pathContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;



final class LeafRefPathParserListenerImpl extends LeafRefPathParserBaseListener{

    private final SchemaContext schemaContext;
    private final Module module;
    private LeafRefPath leafRefPath;
    private boolean relativePath=false;
    private QNameWithPredicateBuilder currentLeafRefPathQName;
    private QNamePredicateBuilder currentPredicate;
    private QNameModule currentQnameModule;
    private String currentQNameLocalName;
    private final List<QNameWithPredicateBuilder> leafRefPathQnameList;
    private List<QNameWithPredicateBuilder> predicatePathKeyQnameList;
    private final SchemaNode node; //FIXME use for identifier path completion
    private ParsingState currentParsingState;

    Function<QNameWithPredicateBuilder, QNameWithPredicate> build = new Function<QNameWithPredicateBuilder, QNameWithPredicate>() {
        @Override
        public QNameWithPredicate apply(final QNameWithPredicateBuilder builder) {
           return builder.build();
        }
     };

    private enum ParsingState {
        LEAF_REF_PATH, PATH_PREDICATE, PREDICATE_PATH_EQUALITY_EXPR, PATH_KEY_EXPR
    }

    public LeafRefPathParserListenerImpl(final SchemaContext schemaContext, final Module currentModule, final SchemaNode currentNode) {
       this.schemaContext = schemaContext;
       this.module = currentModule;
       this.leafRefPathQnameList = new LinkedList<QNameWithPredicateBuilder>();
       this.node=currentNode;
       this.currentParsingState = ParsingState.LEAF_REF_PATH;
    }

    @Override
    public void enterPath_predicate(final Path_predicateContext ctx) {
        currentParsingState=ParsingState.PATH_PREDICATE;
        currentPredicate = new QNamePredicateBuilder();
    }


    @Override
    public void exitPath_predicate(final Path_predicateContext ctx) {
        currentLeafRefPathQName.addQNamePredicate(currentPredicate.build());
        currentPredicate = null;
        currentParsingState=ParsingState.LEAF_REF_PATH;
    }


    @Override
    public void enterRel_path_keyexpr(final Rel_path_keyexprContext ctx) {
        currentParsingState=ParsingState.PATH_KEY_EXPR;
        predicatePathKeyQnameList = new LinkedList<QNameWithPredicateBuilder>();
        final List<TerminalNode> dots = ctx.DOTS();
        for (final TerminalNode parent : dots) {
            predicatePathKeyQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }
    }


    @Override
    public void exitRel_path_keyexpr(final Rel_path_keyexprContext ctx) {

        final LeafRefPath pathKeyExpression = LeafRefPath.create(Lists.transform(predicatePathKeyQnameList, build), false);
        currentPredicate.setPathKeyExpression(pathKeyExpression);

        currentParsingState=ParsingState.PREDICATE_PATH_EQUALITY_EXPR;
    }

    @Override
    public void enterRelative_path(final Relative_pathContext ctx) {

        relativePath = true;
        final List<TerminalNode> dots = ctx.DOTS();
        for (final TerminalNode parent : dots) {
            leafRefPathQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }

    }

    @Override
    public void enterPath_equality_expr(final Path_equality_exprContext ctx) {
        currentParsingState=ParsingState.PREDICATE_PATH_EQUALITY_EXPR;
    }


    @Override
    public void exitPath_equality_expr(final Path_equality_exprContext ctx) {

        currentParsingState=ParsingState.PATH_PREDICATE;
    }

    @Override
    public void enterPrefix(final PrefixContext ctx) {

        if (module.getPrefix().equals(ctx.getText())) {
            currentQnameModule = module.getQNameModule();
        } else {
            currentQnameModule = getQNameModuleForImportPrefix(ctx.getText());
        }
    }

    @Override
    public void exitPath_arg(final Path_argContext ctx) {
        leafRefPath = LeafRefPath.create(Lists.transform(leafRefPathQnameList,build), !relativePath);
    }


    @Override
    public void enterIdentifier(final IdentifierContext ctx) {
        currentQNameLocalName = ctx.getText();
    }

    @Override
    public void exitNode_identifier(final Node_identifierContext ctx) {

        if (currentQnameModule == null) {
            currentQnameModule = module.getQNameModule();
        }

        if (currentParsingState == ParsingState.PREDICATE_PATH_EQUALITY_EXPR) {
            final QName qname = QName.create(currentQnameModule,
                    currentQNameLocalName);
            currentPredicate.setIdentifier(qname);
        } else {

            final QNameWithPredicateBuilder qnameBuilder = new QNameWithPredicateBuilder(
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


    private URI getNamespaceForImportPrefix(final String prefix){
        final ModuleImport moduleImport = getModuleImport(prefix);
        final Module findedModule = schemaContext.findModuleByName(moduleImport.getModuleName(), moduleImport.getRevision());

        return findedModule.getNamespace();
    }

    private QNameModule getQNameModuleForImportPrefix(final String prefix) {
        final ModuleImport moduleImport = getModuleImport(prefix);

        if (moduleImport == null) {
            throw new LeafRefPathParseException("No module import for prefix: "
                    + prefix + " in module: " + module.getName());
        }

        final String moduleName = moduleImport.getModuleName();
        final Date revision = moduleImport.getRevision();
        final Module findedModule = schemaContext.findModuleByName(moduleName,
                revision);

        return findedModule.getQNameModule();
    }


    private ModuleImport getModuleImport(final String prefix) {
        final Set<ModuleImport> imports = module.getImports();

        for (final ModuleImport moduleImport : imports) {
            if(moduleImport.getPrefix().equals(prefix)) {
                return moduleImport;
            }
        }
        return null;
    }

}
