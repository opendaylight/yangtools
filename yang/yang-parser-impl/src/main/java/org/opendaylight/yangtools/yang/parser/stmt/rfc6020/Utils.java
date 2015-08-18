/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;

import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import java.util.Date;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineStatement;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher.is('\'');

    private static final char SEPARATOR_NODENAME = '/';

    private static final String REGEX_PATH_ABS = "/[^/].*";

    public static final char SEPARATOR = ' ';

    private Utils() {
    }

    public static Collection<SchemaNodeIdentifier.Relative> transformKeysStringToKeyNodes(StmtContext<?, ?, ?> ctx,
            String value) {
        Splitter keySplitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();
        List<String> keyTokens = keySplitter.splitToList(value);

        // to detect if key contains duplicates
        if ((new HashSet<>(keyTokens)).size() < keyTokens.size()) {
            throw new IllegalArgumentException();
        }

        Set<SchemaNodeIdentifier.Relative> keyNodes = new HashSet<>();

        for (String keyToken : keyTokens) {

            SchemaNodeIdentifier.Relative keyNode = (Relative) SchemaNodeIdentifier.Relative.create(false,
                    Utils.qNameFromArgument(ctx, keyToken));
            keyNodes.add(keyNode);
        }

        return keyNodes;
    }

    public static List<String> splitPathToNodeNames(String path) {

        Splitter keySplitter = Splitter.on(SEPARATOR_NODENAME).omitEmptyStrings().trimResults();
        return keySplitter.splitToList(path);
    }

    public static void validateXPath(final StmtContext<?, ?, ?> ctx, String path) {

        final XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            xPath.compile(path);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(String.format("Argument %s is not valid XPath string at %s", path, ctx
                    .getStatementSourceReference().toString()), e);
        }
    }

    private static String trimSingleLastSlashFromXPath(final String path) {
        return path.replaceAll("/$", "");
    }

    public static boolean isXPathAbsolute(final StmtContext<?, ?, ?> ctx, String path) {

        validateXPath(ctx, trimSingleLastSlashFromXPath(path));

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
                } else {
                    if (stmtDef.get(new QName(YangConstants.RFC6020_YIN_NAMESPACE, localName)) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Iterable<QName> parseXPath(StmtContext<?, ?, ?> ctx, String path) {

        String trimmedPath = trimSingleLastSlashFromXPath(path);

        validateXPath(ctx, trimmedPath);

        List<String> nodeNames = splitPathToNodeNames(trimmedPath);
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

        if (value == null || value.equals("")) {
            return ctx.getPublicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qNameModule = null;
        String localName = null;

        String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];
            qNameModule = getRootModuleQName(ctx);
            break;
        default:
            prefix = namesParts[0];
            localName = namesParts[1];
            qNameModule = getModuleQNameByPrefix(ctx, prefix);
            // in case of unknown statement argument, we're not going to parse it
            if (qNameModule == null
                    && ctx.getPublicDefinition().getDeclaredRepresentationClass()
                    .isAssignableFrom(UnknownStatementImpl.class)) {
                localName = value;
                qNameModule = getRootModuleQName(ctx);
            }
            if (qNameModule == null
                    && Iterables.getLast(ctx.getCopyHistory()) == StmtContext.TypeOfCopy.ADDED_BY_AUGMENTATION) {
                ctx = ctx.getOriginalCtx();
                qNameModule = getModuleQNameByPrefix(ctx, prefix);
            }
            break;
        }

        if (qNameModule == null) {
            throw new IllegalArgumentException("Error in module '" + ctx.getRoot().rawStatementArgument()
                    + "': can not resolve QNameModule for '" + value + "'.");
        }

        QNameModule resultQNameModule = qNameModule.getRevision() == null ? QNameModule.create(
                qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV) : qNameModule;

        return QName.create(resultQNameModule, localName);
    }

    public static QNameModule getModuleQNameByPrefix(StmtContext<?, ?, ?> ctx, String prefix) {
        QNameModule qNameModule;
        ModuleIdentifier impModIdentifier = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class, prefix);
        qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, impModIdentifier);

        if (qNameModule == null && StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            qNameModule = ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }
        return qNameModule;
    }

    public static QNameModule getRootModuleQName(StmtContext<?, ?, ?> ctx) {

        if (ctx == null) {
            return null;
        }

        StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        QNameModule qNameModule = null;

        if (StmtContextUtils.producesDeclared(rootCtx, ModuleStatement.class)) {
            qNameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (StmtContextUtils.producesDeclared(rootCtx, SubmoduleStatement.class)) {
            String belongsToModuleName = firstAttributeOf(rootCtx.substatements(),
                    BelongsToStatement.class);
            qNameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        }

        return qNameModule.getRevision() == null ? QNameModule.create(qNameModule.getNamespace(),
                SimpleDateFormatUtil.DEFAULT_DATE_REV) : qNameModule;
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(StatementContextBase<?, ?, ?> rootStmtCtx,
            final Iterable<QName> path) {

        StatementContextBase<?, ?, ?> parent = rootStmtCtx;

        Iterator<QName> pathIter = path.iterator();
        while (pathIter.hasNext()) {
            QName nextPathQName = pathIter.next();
            StatementContextBase<?, ?, ?> foundSubstatement = getSubstatementByQName(parent, nextPathQName);

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

    public static StatementContextBase<?, ?, ?> getSubstatementByQName(StatementContextBase<?, ?, ?> parent,
            QName nextPathQName) {

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatement = parent.declaredSubstatements();
        Collection<StatementContextBase<?, ?, ?>> effectiveSubstatement = parent.effectiveSubstatements();

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
    public static StatementContextBase<?, ?, ?> findNode(StatementContextBase<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return findNode(rootStmtCtx, node.getPathFromRoot());
    }

    public static SchemaPath getSchemaPath(StmtContext<?, ?, ?> ctx) {

        if (ctx == null) {
            return null;
        }

        Iterator<StmtContext<?, ?, ?>> iteratorFromRoot = ctx.getStmtContextsFromRoot().iterator();

        if (iteratorFromRoot.hasNext()) {
            iteratorFromRoot.next(); // skip root argument
        }

        List<QName> qNamesFromRoot = new LinkedList<>();
        while (iteratorFromRoot.hasNext()) {
            StmtContext<?, ?, ?> nextStmtCtx = iteratorFromRoot.next();
            Object nextStmtArgument = nextStmtCtx.getStatementArgument();
            if (nextStmtArgument instanceof QName) {
                QName qname = (QName) nextStmtArgument;
                if (StmtContextUtils.producesDeclared(nextStmtCtx, UsesStatement.class)) {
                    continue;
                }
                if (StmtContextUtils.producesDeclared(nextStmtCtx.getParentContext(), ChoiceStatement.class)
                        && isSupportedAsShorthandCase(nextStmtCtx)) {
                    qNamesFromRoot.add(qname);
                }
                qNamesFromRoot.add(qname);
            } else if (nextStmtArgument instanceof String) {
                StatementContextBase<?, ?, ?> originalCtx = ctx
                        .getOriginalCtx();
                final QName qName = (originalCtx != null) ? qNameFromArgument(
                        originalCtx, (String) nextStmtArgument)
                        : qNameFromArgument(ctx, (String) nextStmtArgument);
                qNamesFromRoot.add(qName);
            } else if ((StmtContextUtils.producesDeclared(nextStmtCtx, AugmentStatement.class)
                       || StmtContextUtils.producesDeclared(nextStmtCtx, RefineStatement.class))
                    && nextStmtArgument instanceof SchemaNodeIdentifier) {
                addQNamesFromSchemaNodeIdentifierToList(qNamesFromRoot, (SchemaNodeIdentifier) nextStmtArgument);
            } else if (isUnknownNode(nextStmtCtx)) {
                qNamesFromRoot.add(nextStmtCtx.getPublicDefinition().getStatementName());
            } else {
                return SchemaPath.SAME;
            }
        }

        final SchemaPath schemaPath = SchemaPath.create(qNamesFromRoot, true);
        return schemaPath;
    }

    public static boolean isUnknownNode(StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .isAssignableFrom(UnknownStatementImpl.class);
    }

    private static boolean isSupportedAsShorthandCase(StmtContext<?, ?, ?> statementCtx) {

        Collection<?> supportedCaseShorthands = statementCtx.getFromNamespace(ValidationBundlesNamespace.class,
                ValidationBundleType.SUPPORTED_CASE_SHORTHANDS);

        return supportedCaseShorthands == null || supportedCaseShorthands.contains(statementCtx.getPublicDefinition());
    }

    private static void addQNamesFromSchemaNodeIdentifierToList(List<QName> qNamesFromRoot,
            SchemaNodeIdentifier augmentTargetPath) {
        Iterator<QName> augmentTargetPathIterator = augmentTargetPath.getPathFromRoot().iterator();
        while (augmentTargetPathIterator.hasNext()) {
            qNamesFromRoot.add(augmentTargetPathIterator.next());
        }
    }

    public static Deviation.Deviate parseDeviateFromString(final String deviate) {

        // Yang constants should be lowercase so we have throw if value does not
        // suit this
        String deviateUpper = deviate.toUpperCase();
        if (Objects.equals(deviate, deviateUpper)) {
            throw new IllegalArgumentException(String.format("String %s is not valid deviate argument", deviate));
        }

        // but Java enum is uppercase so we cannot use lowercase here
        try {
            return Deviation.Deviate.valueOf(deviateUpper);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("String %s is not valid deviate argument", deviate), e);
        }
    }

    public static Status parseStatus(String value) {

        Status status = null;
        switch (value) {
        case "current":
            status = Status.CURRENT;
            break;
        case "deprecated":
            status = Status.DEPRECATED;
            break;
        case "obsolete":
            status = Status.OBSOLETE;
            break;
        default:
            LOG.warn("Invalid 'status' statement: " + value);
        }

        return status;
    }

    public static SchemaPath SchemaNodeIdentifierToSchemaPath(SchemaNodeIdentifier identifier) {
        return SchemaPath.create(identifier.getPathFromRoot(), identifier.isAbsolute());
    }

    public static Date getLatestRevision(RootStatementContext<?, ?, ?> root) {
        return getLatestRevision(root.declaredSubstatements());
    }

    public static Date getLatestRevision(Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Date revision = null;
        for (StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.getPublicDefinition().getDeclaredRepresentationClass().isAssignableFrom(RevisionStatement
                    .class)) {
                if (revision == null && subStmt.getStatementArgument() != null) {
                    revision = (Date) subStmt.getStatementArgument();
                } else if (subStmt.getStatementArgument() != null && ((Date) subStmt.getStatementArgument()).compareTo
                        (revision) > 0) {
                    revision = (Date) subStmt.getStatementArgument();
                }
            }
        }
        return revision;
    }

    public static boolean isModuleIdentifierWithoutSpecifiedRevision(Object o) {
        return (o instanceof ModuleIdentifier)
                && (((ModuleIdentifier) o).getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP ||
                        ((ModuleIdentifier) o).getRevision() == SimpleDateFormatUtil.DEFAULT_BELONGS_TO_DATE);
    }
}
