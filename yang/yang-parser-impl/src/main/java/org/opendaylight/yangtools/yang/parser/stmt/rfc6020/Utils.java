/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.util.Collection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.model.api.Deviation;
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
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher
            .is('\'');

    private static final char SEPARATOR_NODENAME = '/';

    private static final String REGEX_PATH_ABS = "/[^/].*";

    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL64 = "decimal64";
    public static final String EMPTY = "empty";
    public static final String INT8 = "int8";
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    public static final String STRING = "string";
    public static final String UINT8 = "uint8";
    public static final String UINT16 = "uint16";
    public static final String UINT32 = "uint32";
    public static final String UINT64 = "uint64";

    private static final Set<String> STATEMENT_BUILD_IN_TYPES = ImmutableSet.<String> builder()
            .add(BOOLEAN)
            .add(DECIMAL64)
            .add(EMPTY)
            .add(INT8)
            .add(INT16)
            .add(INT32)
            .add(INT64)
            .add(STRING)
            .add(UINT8)
            .add(UINT16)
            .add(UINT32)
            .add(UINT64)
            .build();

    private Utils() {
    }

    public static List<String> splitPathToNodeNames(String path) {

        Splitter keySplitter = Splitter.on(SEPARATOR_NODENAME)
                .omitEmptyStrings().trimResults();
        return keySplitter.splitToList(path);
    }

    public static void validateXPath(String path) {

        final XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            xPath.compile(path);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(
                    "Argument is not valid XPath string", e);
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

    public static String getPrefixFromArgument(String prefixedLocalName) {
        String[] namesParts = prefixedLocalName.split(":");
        if (namesParts.length == 2) {
            return namesParts[0];
        }
        return null;
    }

    public static boolean isValidStatementDefinition(PrefixToModule prefixes,
            QNameToStatementDefinition stmtDef, QName identifier) {
        if (stmtDef.get(identifier) != null) {
            return true;
        } else {
            String prefixedLocalName = identifier.getLocalName();
            String[] namesParts = prefixedLocalName.split(":");

            if (namesParts.length == 2) {
                String prefix = namesParts[0];
                String localName = namesParts[1];
                if (prefixes != null && prefixes.get(prefix) != null && stmtDef.get(new QName(YangConstants.RFC6020_YIN_NAMESPACE, localName)) != null) {
                    return true;
                } else {
                    if (stmtDef.get(new QName(YangConstants.RFC6020_YIN_NAMESPACE, localName)) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Iterable<QName> parseXPath(StmtContext<?, ?, ?> ctx,
            String path) {

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

    public static String stringFromStringContext(
            final YangStatementParser.ArgumentContext context) {
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

            if (StmtContextUtils.producesDeclared(ctx.getRoot(),
                    ModuleStatement.class)) {
                prefix = firstAttributeOf(
                        ctx.getRoot().declaredSubstatements(),
                        PrefixStatement.class);
                qNameModule = ctx
                        .getFromNamespace(PrefixToModule.class, prefix);

            } else if (StmtContextUtils.producesDeclared(ctx.getRoot(),
                    SubmoduleStatement.class)) {
                String belongsToModuleName = firstAttributeOf(ctx.getRoot()
                        .declaredSubstatements(), BelongsToStatement.class);
                qNameModule = ctx.getFromNamespace(
                        ModuleNameToModuleQName.class, belongsToModuleName);
            }
            break;
        case 2:
            prefix = namesParts[0];
            localName = namesParts[1];

            ModuleIdentifier impModIdentifier = ctx.getRoot().getFromNamespace(
                    ImpPrefixToModuleIdentifier.class, prefix);
            qNameModule = ctx.getFromNamespace(
                    ModuleIdentifierToModuleQName.class, impModIdentifier);

            if (qNameModule == null
                    && StmtContextUtils.producesDeclared(ctx.getRoot(),
                            SubmoduleStatement.class)) {
                String moduleName = ctx.getRoot().getFromNamespace(
                        BelongsToPrefixToModuleName.class, prefix);
                qNameModule = ctx.getFromNamespace(
                        ModuleNameToModuleQName.class, moduleName);
            }

            break;
        default:
            break;
        }

        return QName.create(qNameModule, localName);
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInSubstatements(
            StatementContextBase<?, ?, ?> rootStmtCtx,
            final Iterable<QName> path) {

        StatementContextBase<?, ?, ?> parent = rootStmtCtx;

        Iterator<QName> pathIter = path.iterator();
        while (pathIter.hasNext()) {
            QName nextPathQName = pathIter.next();
            StatementContextBase<?, ?, ?> foundSubstatement = getSubstatementByQName(
                    parent, nextPathQName);

            if (foundSubstatement == null) {
                return null;
            }
            if (!pathIter.hasNext()) {
                return foundSubstatement;
            }

            parent = foundSubstatement;
        }

        return null;
    }

    public static StatementContextBase<?, ?, ?> getSubstatementByQName(
            StatementContextBase<?, ?, ?> parent, QName nextPathQName) {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatement = parent
                .declaredSubstatements();
        Collection<StatementContextBase<?, ?, ?>> effectiveSubstatement = parent
                .effectiveSubstatements();

        Collection<StatementContextBase<?, ?, ?>> allSubstatements = new LinkedList<>();
        allSubstatements.addAll(declaredSubstatement);
        allSubstatements.addAll(effectiveSubstatement);

        for (StatementContextBase<?, ?, ?> substatement : allSubstatements) {
            if (nextPathQName.equals(substatement.getStatementArgument())) {
                return substatement;
            }
        }

        return null;
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findCtxOfNodeInRoot(
            StatementContextBase<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return findCtxOfNodeInSubstatements(rootStmtCtx, node.getPathFromRoot());
    }

    public static SchemaPath getSchemaPath(StmtContext<?, ?, ?> ctx) {

        Iterator<Object> argumentsIterator = ctx.getArgumentsFromRoot()
                .iterator();
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

    public static Deviation.Deviate parseDeviateFromString(final String deviate) {
        if ("not-supported".equals(deviate)) {
            return Deviation.Deviate.NOT_SUPPORTED;
        } else if ("add".equals(deviate)) {
            return Deviation.Deviate.ADD;
        } else if ("replace".equals(deviate)) {
            return Deviation.Deviate.REPLACE;
        } else if ("delete".equals(deviate)) {
            return Deviation.Deviate.DELETE;
        } else {
            throw new IllegalArgumentException(
                    "String %s is not valid deviate argument");
        }
    }

    public static boolean isYangStatementBuildInType(final String type) {
        return STATEMENT_BUILD_IN_TYPES.contains(type);
    }
}
