/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public final class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher.is('\'');

    private static final char SEPARATOR_NODENAME = '/';

    private static final String REGEX_PATH_ABS = "/[^/].*";

    private Utils() {
    }

    public static List<String> splitPathToNodeNames(String path) {

        Splitter keySplitter = Splitter.on(SEPARATOR_NODENAME).omitEmptyStrings().trimResults();
        return keySplitter.splitToList(path);
    }

    public static void validateXPath(String path) {

        final XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            xPath.compile(path);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("Argument is not valid XPath string", e);
        }
    }

    public static boolean isXPathAbsolute(String path) {

        validateXPath(path);

        return path.matches(REGEX_PATH_ABS);
    }

    public static QName trimPrefix(QName identifier) {
        String prefixedLocalName = identifier.getLocalName();
        String[] namesParts = prefixedLocalName.split(":");

        if (namesParts.length == 2) {
            String localName = namesParts[1];
            return QName.create(identifier.getModule(), localName);
        }

        return identifier;
    }

    public static boolean isValidStatementDefinition(PrefixToModule prefixes, QNameToStatementDefinition stmtDef,
            QName identifier) {
        if (stmtDef.get(identifier) != null) {
            return true;
        } else {
            String prefixedLocalName = identifier.getLocalName();
            String[] namesParts = prefixedLocalName.split(":");

            if (namesParts.length == 2) {
                String prefix = namesParts[0];
                String localName = namesParts[1];
                if (prefixes != null && prefixes.get(prefix) != null
                        && stmtDef.get(new QName(YangConstants.RFC6020_YIN_NAMESPACE, localName)) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Iterable<QName> parseXPath(StmtContext<?, ?, ?> ctx, String path) {

        validateXPath(path);

        List<String> nodeNames = splitPathToNodeNames(path);
        List<QName> qNames = new ArrayList<>();

        for (String nodeName : nodeNames) {
            try {
                final QName qName = Utils.qNameFromArgument(ctx, nodeName);
                qNames.add(qName);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        return qNames;
    }

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context) {
        StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.isEmpty()) {
            strings = Arrays.asList(context.IDENTIFIER());
        }
        for (TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            char firstChar = str.charAt(0);
            final CharMatcher quoteMatcher;
            if (SINGLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = SINGLE_QUOTE_MATCHER;
            } else if (DOUBLE_QUOTE_MATCHER.matches(firstChar)) {
                quoteMatcher = DOUBLE_QUOTE_MATCHER;
            } else {
                sb.append(str);
                continue;
            }
            sb.append(quoteMatcher.removeFrom(str.substring(1, str.length() - 1)));
        }
        return sb.toString();
    }

    public static QName qNameFromArgument(StmtContext<?, ?, ?> ctx, String value) {

        String prefix;
        QNameModule qNameModule = null;
        try {
            qNameModule = QNameModule.create(new URI(""), new Date(0));
        } catch (URISyntaxException e) {
            LOG.warn(e.getMessage(), e);
        }
        String localName = null;

        String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];

            if (StmtContextUtils.producesDeclared(ctx.getRoot(), ModuleStatement.class)) {
                prefix = firstAttributeOf(ctx.getRoot().declaredSubstatements(), PrefixStatement.class);
                qNameModule = ctx.getFromNamespace(PrefixToModule.class, prefix);

            } else if (StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
                String belongsToModuleName = firstAttributeOf(ctx.getRoot().declaredSubstatements(),
                        BelongsToStatement.class);
                qNameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
            }
            break;
        case 2:
            prefix = namesParts[0];
            localName = namesParts[1];

            ModuleIdentifier impModIdentifier = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class,
                    prefix);
            qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, impModIdentifier);

            if (qNameModule == null && StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
                String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
                qNameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
            }

            break;
        default:
            break;
        }

        return QName.create(qNameModule, localName);
    }

    @Nullable
    private static StatementContextBase<?, ?, ?> findCtxOfNodeInSubstatements(
            StatementContextBase<?, ?, ?> rootStmtCtx, final Iterable<QName> path, boolean searchInEffective) {

        StatementContextBase<?, ?, ?> parent = rootStmtCtx;

        Iterator<QName> pathIter = path.iterator();
        QName targetNode = pathIter.next();

        while (pathIter.hasNext()) {

            for (StatementContextBase<?, ?, ?> child : searchInEffective ? parent.effectiveSubstatements() : parent
                    .declaredSubstatements()) {

                if (targetNode.equals(child.getStatementArgument())) {
                    parent = child;
                    targetNode = pathIter.next();
                }
            }

            if (parent.equals(rootStmtCtx)) {

                return null;
            }
        }

        StatementContextBase<?, ?, ?> targetCtx = null;

        for (StatementContextBase<?, ?, ?> child : searchInEffective ? parent.effectiveSubstatements() : parent
                .declaredSubstatements()) {

            if (targetNode.equals(child.getStatementArgument())) {
                targetCtx = child;
            }
        }

        return targetCtx;
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInRoot(StatementContextBase<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {

        System.out.println(node.getPathFromRoot());

        StatementContextBase<?, ?, ?> targetCtx = findCtxOfNodeInSubstatements(rootStmtCtx, node.getPathFromRoot(),
                false);

        if (targetCtx == null) {

            targetCtx = findCtxOfNodeInSubstatements(rootStmtCtx, node.getPathFromRoot(), true);
        }

        return targetCtx;
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInRoot(StatementContextBase<?, ?, ?> rootStmtCtx,
            final Iterable<QName> path) {

        StatementContextBase<?, ?, ?> targetCtx = findCtxOfNodeInSubstatements(rootStmtCtx, path, false);

        if (targetCtx == null) {

            targetCtx = findCtxOfNodeInSubstatements(rootStmtCtx, path, true);
        }

        return targetCtx;
    }

    public static SchemaPath getSchemaPath(StmtContext<?, ?, ?> ctx) {

        Iterator<Object> argumentsIterator = ctx.getArgumentsFromRoot().iterator();
        argumentsIterator.next(); // skip root argument

        List<QName> qNamesFromRoot = new LinkedList<>();

        while (argumentsIterator.hasNext()) {
            Object argument = argumentsIterator.next();
            if (argument instanceof QName) {
                QName qname = (QName) argument;
                qNamesFromRoot.add(qname);
            } else {
                return SchemaPath.SAME;
            }
        }

        return SchemaPath.create(qNamesFromRoot, true);
    }
}
