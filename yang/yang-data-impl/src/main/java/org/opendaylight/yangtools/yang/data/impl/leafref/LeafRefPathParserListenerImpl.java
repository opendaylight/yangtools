/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
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

final class LeafRefPathParserListenerImpl extends LeafRefPathParserBaseListener {

    private final List<QNameWithPredicateBuilder> leafRefPathQnameList = new ArrayList<>();
    private final SchemaContext schemaContext;
    private final Module module;
    private final Module leafrefModule;
    // FIXME: use for identifier path completion
    private final SchemaNode node;

    private ParsingState currentParsingState = ParsingState.LEAF_REF_PATH;
    private List<QNameWithPredicateBuilder> predicatePathKeyQnameList;
    private QNameWithPredicateBuilder currentLeafRefPathQName;
    private QNamePredicateBuilder currentPredicate;
    private QNameModule currentQnameModule;
    private String currentQNameLocalName;
    private LeafRefPath leafRefPath;
    private boolean relativePath = false;

    private enum ParsingState {
        LEAF_REF_PATH, PATH_PREDICATE, PREDICATE_PATH_EQUALITY_EXPR, PATH_KEY_EXPR
    }

    LeafRefPathParserListenerImpl(final SchemaContext schemaContext, final Module leafrefModule,
            final SchemaNode currentNode) {
        this.schemaContext = schemaContext;
        this.module = schemaContext.findModule(currentNode.getQName().getModule()).get();
        this.leafrefModule = leafrefModule;
        this.node = currentNode;
    }

    @Override
    public void enterPath_predicate(final Path_predicateContext ctx) {
        currentParsingState = ParsingState.PATH_PREDICATE;
        currentPredicate = new QNamePredicateBuilder();
    }

    @Override
    public void exitPath_predicate(final Path_predicateContext ctx) {
        currentLeafRefPathQName.addQNamePredicate(currentPredicate.build());
        currentPredicate = null;
        currentParsingState = ParsingState.LEAF_REF_PATH;
    }


    @Override
    public void enterRel_path_keyexpr(final Rel_path_keyexprContext ctx) {
        currentParsingState = ParsingState.PATH_KEY_EXPR;

        final List<TerminalNode> dots = ctx.DOTS();
        predicatePathKeyQnameList = new ArrayList<>(dots.size());
        for (int i = 0; i < dots.size(); ++i) {
            predicatePathKeyQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }
    }

    @Override
    public void exitRel_path_keyexpr(final Rel_path_keyexprContext ctx) {
        final LeafRefPath pathKeyExpression = LeafRefPath.create(Lists.transform(predicatePathKeyQnameList,
            QNameWithPredicateBuilder::build), false);
        currentPredicate.setPathKeyExpression(pathKeyExpression);

        currentParsingState = ParsingState.PREDICATE_PATH_EQUALITY_EXPR;
    }

    @Override
    public void enterRelative_path(final Relative_pathContext ctx) {
        relativePath = true;
        final List<TerminalNode> dots = ctx.DOTS();
        for (int i = 0; i < dots.size(); ++i) {
            leafRefPathQnameList.add(QNameWithPredicateBuilder.UP_PARENT_BUILDER);
        }
    }

    @Override
    public void enterPath_equality_expr(final Path_equality_exprContext ctx) {
        currentParsingState = ParsingState.PREDICATE_PATH_EQUALITY_EXPR;
    }

    @Override
    public void exitPath_equality_expr(final Path_equality_exprContext ctx) {
        currentParsingState = ParsingState.PATH_PREDICATE;
    }

    @Override
    public void enterPrefix(final PrefixContext ctx) {
        final String prefix = ctx.getText();
        if (!leafrefModule.getPrefix().equals(prefix)) {
            final Optional<QNameModule> qnameModuleOpt = getQNameModuleForImportPrefix(leafrefModule, prefix);
            checkArgument(qnameModuleOpt.isPresent(), "No module import for prefix: %s in module: %s", prefix,
                leafrefModule.getName());
            currentQnameModule = qnameModuleOpt.get();
        } else {
            currentQnameModule = leafrefModule.getQNameModule();
        }
    }

    @Override
    public void exitPath_arg(final Path_argContext ctx) {
        leafRefPath = LeafRefPath.create(Lists.transform(leafRefPathQnameList, QNameWithPredicateBuilder::build),
            !relativePath);
    }

    @Override
    public void enterIdentifier(final IdentifierContext ctx) {
        currentQNameLocalName = ctx.getText();
    }

    @Override
    public void exitNode_identifier(final Node_identifierContext ctx) {
        if (currentQnameModule == null) {
            if (relativePath) {
                currentQnameModule = module.getQNameModule();
            } else {
                currentQnameModule = leafrefModule.getQNameModule();
            }
        }

        if (currentParsingState == ParsingState.PREDICATE_PATH_EQUALITY_EXPR) {
            currentPredicate.setIdentifier(QName.create(currentQnameModule, currentQNameLocalName));
        } else {
            final QNameWithPredicateBuilder qnameBuilder = new QNameWithPredicateBuilder(currentQnameModule,
                currentQNameLocalName);

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

    private Optional<QNameModule> getQNameModuleForImportPrefix(final Module targetModule, final String prefix) {
        final ModuleImport moduleImport = getModuleImport(targetModule, prefix);
        if (moduleImport == null) {
            return Optional.empty();
        }

        final String moduleName = moduleImport.getModuleName();
        final Optional<Revision> revision = moduleImport.getRevision();
        return schemaContext.findModule(moduleName, revision).map(Module::getQNameModule);
    }

    private static ModuleImport getModuleImport(final Module targetModule, final String prefix) {
        return targetModule.getImports().stream()
            .filter(imp -> prefix.equals(imp.getPrefix())).findFirst().orElse(null);
    }
}
