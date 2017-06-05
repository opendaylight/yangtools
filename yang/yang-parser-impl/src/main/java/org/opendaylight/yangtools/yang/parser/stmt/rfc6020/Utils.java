/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_NAMESPACE;
import static org.opendaylight.yangtools.yang.common.YangConstants.YANG_XPATH_FUNCTIONS_PREFIX;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.RegEx;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher LEFT_PARENTHESIS_MATCHER = CharMatcher.is('(');
    private static final CharMatcher RIGHT_PARENTHESIS_MATCHER = CharMatcher.is(')');
    private static final CharMatcher AMPERSAND_MATCHER = CharMatcher.is('&');
    private static final CharMatcher QUESTION_MARK_MATCHER = CharMatcher.is('?');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final Splitter COLON_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults();
    private static final Pattern PATH_ABS = Pattern.compile("/[^/].*");
    @RegEx
    private static final String YANG_XPATH_FUNCTIONS_STRING =
            "(re-match|deref|derived-from(-or-self)?|enum-value|bit-is-set)(\\()";
    private static final Pattern YANG_XPATH_FUNCTIONS_PATTERN = Pattern.compile(YANG_XPATH_FUNCTIONS_STRING);

    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {
            return XPathFactory.newInstance();
        }
    };

    private Utils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Cleanup any resources attached to the current thread. Threads interacting with this class can cause thread-local
     * caches to them. Invoke this method if you want to detach those resources.
     */
    public static void detachFromCurrentThread() {
        XPATH_FACTORY.remove();
    }

    public static Collection<SchemaNodeIdentifier.Relative> transformKeysStringToKeyNodes(final StmtContext<?, ?, ?> ctx,
            final String value) {
        final List<String> keyTokens = SPACE_SPLITTER.splitToList(value);

        // to detect if key contains duplicates
        if (new HashSet<>(keyTokens).size() < keyTokens.size()) {
            // FIXME: report all duplicate keys
            throw new SourceException(ctx.getStatementSourceReference(), "Duplicate value in list key: %s", value);
        }

        final Set<SchemaNodeIdentifier.Relative> keyNodes = new HashSet<>();

        for (final String keyToken : keyTokens) {

            final SchemaNodeIdentifier.Relative keyNode = (Relative) SchemaNodeIdentifier.Relative.create(false,
                    Utils.qNameFromArgument(ctx, keyToken));
            keyNodes.add(keyNode);
        }

        return keyNodes;
    }

    static Collection<SchemaNodeIdentifier.Relative> parseUniqueConstraintArgument(final StmtContext<?, ?, ?> ctx,
            final String argumentValue) {
        final Set<SchemaNodeIdentifier.Relative> uniqueConstraintNodes = new HashSet<>();
        for (final String uniqueArgToken : SPACE_SPLITTER.split(argumentValue)) {
            final SchemaNodeIdentifier nodeIdentifier = Utils.nodeIdentifierFromPath(ctx, uniqueArgToken);
            SourceException.throwIf(nodeIdentifier.isAbsolute(), ctx.getStatementSourceReference(),
                    "Unique statement argument '%s' contains schema node identifier '%s' "
                            + "which is not in the descendant node identifier form.", argumentValue, uniqueArgToken);
            uniqueConstraintNodes.add((SchemaNodeIdentifier.Relative) nodeIdentifier);
        }
        return ImmutableSet.copyOf(uniqueConstraintNodes);
    }

    private static String trimSingleLastSlashFromXPath(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
        final XPath xPath = XPATH_FACTORY.get().newXPath();
        xPath.setNamespaceContext(StmtNamespaceContext.create(ctx,
                ImmutableBiMap.of(RFC6020_YANG_NAMESPACE.toString(), YANG_XPATH_FUNCTIONS_PREFIX)));

        final String trimmed = trimSingleLastSlashFromXPath(path);
        try {
            // XPath extension functions have to be prefixed
            // yang-specific XPath functions are in fact extended functions, therefore we have to add
            // "yang" prefix to them so that they can be properly validated with the XPath.compile() method
            // the "yang" prefix is bound to RFC6020 YANG namespace
            final String prefixedXPath = addPrefixToYangXPathFunctions(trimmed, ctx);
            // TODO: we could capture the result and expose its 'evaluate' method
            xPath.compile(prefixedXPath);
        } catch (final XPathExpressionException e) {
            LOG.warn("Argument \"{}\" is not valid XPath string at \"{}\"", path, ctx.getStatementSourceReference(), e);
        }

        return new RevisionAwareXPathImpl(path, PATH_ABS.matcher(path).matches());
    }

    private static String addPrefixToYangXPathFunctions(final String path, final StmtContext<?, ?, ?> ctx) {
        if (ctx.getRootVersion() == YangVersion.VERSION_1_1) {
            // FIXME once Java 9 is available, change this to StringBuilder as Matcher.appendReplacement() and
            // Matcher.appendTail() will accept StringBuilder parameter in Java 9
            final StringBuffer result = new StringBuffer();
            final String prefix = YANG_XPATH_FUNCTIONS_PREFIX + ":";
            final Matcher matcher = YANG_XPATH_FUNCTIONS_PATTERN.matcher(path);
            while (matcher.find()) {
                matcher.appendReplacement(result, prefix + matcher.group());
            }

            matcher.appendTail(result);
            return result.toString();
        }

        return path;
    }

    public static QName trimPrefix(final QName identifier) {
        final String prefixedLocalName = identifier.getLocalName();
        final String[] namesParts = prefixedLocalName.split(":");

        if (namesParts.length == 2) {
            final String localName = namesParts[1];
            return QName.create(identifier.getModule(), localName);
        }

        return identifier;
    }

    public static String trimPrefix(final String identifier) {
        final List<String> namesParts = COLON_SPLITTER.splitToList(identifier);
        if (namesParts.size() == 2) {
            return namesParts.get(1);
        }
        return identifier;
    }

    static SchemaNodeIdentifier nodeIdentifierFromPath(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: is the path trimming really necessary??
        final List<QName> qNames = new ArrayList<>();
        for (final String nodeName : SLASH_SPLITTER.split(trimSingleLastSlashFromXPath(path))) {
            try {
                final QName qName = Utils.qNameFromArgument(ctx, nodeName);
                qNames.add(qName);
            } catch (final RuntimeException e) {
                throw new SourceException(ctx.getStatementSourceReference(), e,
                        "Failed to parse node '%s' in path '%s'", nodeName, path);
            }
        }

        return SchemaNodeIdentifier.create(qNames, PATH_ABS.matcher(path).matches());
    }

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context,
            final StatementSourceReference ref) {
        return stringFromStringContext(context, YangVersion.VERSION_1, ref);
    }

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context,
            final YangVersion yangVersion, final StatementSourceReference ref) {
        final StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.isEmpty()) {
            strings = Collections.singletonList(context.IDENTIFIER());
        }
        for (final TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            final char firstChar = str.charAt(0);
            final char lastChar = str.charAt(str.length() - 1);
            if (firstChar == '"' && lastChar == '"') {
                final String innerStr = str.substring(1, str.length() - 1);
                /*
                 * Unescape escaped double quotes, tabs, new line and backslash
                 * in the inner string and trim the result.
                 */
                checkDoubleQuotedString(innerStr, yangVersion, ref);
                sb.append(innerStr.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
                        .replace("\\t", "\t"));
            } else if (firstChar == '\'' && lastChar == '\'') {
                /*
                 * According to RFC6020 a single quote character cannot occur in
                 * a single-quoted string, even when preceded by a backslash.
                 */
                sb.append(str.substring(1, str.length() - 1));
            } else {
                checkUnquotedString(str, yangVersion, ref);
                sb.append(str);
            }
        }
        return sb.toString();
    }

    private static void checkUnquotedString(final String str, final YangVersion yangVersion,
            final StatementSourceReference ref) {
        if (yangVersion == YangVersion.VERSION_1_1) {
            for (int i = 0; i < str.length(); i++) {
                switch (str.charAt(i)) {
                case '"':
                case '\'':
                    throw new SourceException(ref, "Yang 1.1: unquoted string (%s) contains illegal characters", str);
                }
            }
        }
    }

    private static void checkDoubleQuotedString(final String str, final YangVersion yangVersion,
            final StatementSourceReference ref) {
        if (yangVersion == YangVersion.VERSION_1_1) {
            for (int i = 0; i < str.length() - 1; i++) {
                if (str.charAt(i) == '\\') {
                    switch (str.charAt(i + 1)) {
                    case 'n':
                    case 't':
                    case '\\':
                    case '\"':
                        i++;
                        break;
                    default:
                        throw new SourceException(ref,
                                "Yang 1.1: illegal double quoted string (%s). In double quoted string the backslash must be followed "
                                        + "by one of the following character [n,t,\",\\], but was '%s'.", str,
                                str.charAt(i + 1));
                    }
                }
            }
        }
    }

    public static QName qNameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.getPublicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qNameModule = null;
        String localName = null;

        final String[] namesParts = value.split(":");
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
                    && ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                ctx = ctx.getOriginalCtx();
                qNameModule = getModuleQNameByPrefix(ctx, prefix);
            }
            break;
        }

        qNameModule = InferenceException.throwIfNull(qNameModule, ctx.getStatementSourceReference(),
            "Cannot resolve QNameModule for '%s'", value);

        final QNameModule resultQNameModule;
        if (qNameModule.getRevision() == null) {
            resultQNameModule = QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV)
                .intern();
        } else {
            resultQNameModule = qNameModule;
        }

        return ctx.getFromNamespace(QNameCacheNamespace.class, QName.create(resultQNameModule, localName));
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final ModuleIdentifier modId = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class, prefix);
        final QNameModule qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, modId);

        if (qNameModule == null && StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            final String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }
        return qNameModule;
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qNameModule;

        if (StmtContextUtils.producesDeclared(rootCtx, ModuleStatement.class)) {
            qNameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (StmtContextUtils.producesDeclared(rootCtx, SubmoduleStatement.class)) {
            final String belongsToModuleName = firstAttributeOf(rootCtx.declaredSubstatements(),
                BelongsToStatement.class);
            qNameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        } else {
            qNameModule = null;
        }

        Preconditions.checkArgument(qNameModule != null, "Failed to look up root QNameModule for %s", ctx);
        if (qNameModule.getRevision() != null) {
            return qNameModule;
        }

        return QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV).intern();
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(final StmtContext<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return (StatementContextBase<?, ?, ?>) rootStmtCtx.getFromNamespace(SchemaNodeIdentifierBuildNamespace.class, node);
    }

    public static boolean isUnknownNode(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx != null && stmtCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .isAssignableFrom(UnknownStatementImpl.class);
    }

    static String internBoolean(final String input) {
        if ("true".equals(input)) {
            return "true";
        } else if ("false".equals(input)) {
            return "false";
        } else {
            return input;
        }
    }

    public static Status parseStatus(final String value) {
        switch (value) {
            case "current":
                return Status.CURRENT;
            case "deprecated":
                return Status.DEPRECATED;
            case "obsolete":
                return Status.OBSOLETE;
            default:
                LOG.warn("Invalid 'status' statement: {}", value);
                return null;
        }
    }

    public static Date getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Date revision = null;
        for (final StmtContext<?, ?, ?> subStmt : subStmts) {
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

    /**
     * Replaces illegal characters of QName by the name of the character (e.g.
     * '?' is replaced by "QuestionMark" etc.).
     *
     * @param string
     *            input String
     * @return result String
     */
    public static String replaceIllegalCharsForQName(String string) {
        string = LEFT_PARENTHESIS_MATCHER.replaceFrom(string, "LeftParenthesis");
        string = RIGHT_PARENTHESIS_MATCHER.replaceFrom(string, "RightParenthesis");
        string = AMPERSAND_MATCHER.replaceFrom(string, "Ampersand");
        string = QUESTION_MARK_MATCHER.replaceFrom(string, "QuestionMark");

        return string;
    }

    public static boolean belongsToTheSameModule(final QName targetStmtQName, final QName sourceStmtQName) {
        return targetStmtQName.getModule().equals(sourceStmtQName.getModule());
    }

    public static SourceIdentifier createSourceIdentifier(final RootStatementContext<?, ?, ?> root) {
        final QNameModule qNameModule = root.getFromNamespace(ModuleCtxToModuleQName.class, root);
        if (qNameModule != null) {
            // creates SourceIdentifier for a module
            return RevisionSourceIdentifier.create((String) root.getStatementArgument(),
                qNameModule.getFormattedRevision());
        }

        // creates SourceIdentifier for a submodule
        final Date revision = Optional.ofNullable(Utils.getLatestRevision(root.declaredSubstatements()))
                .orElse(SimpleDateFormatUtil.DEFAULT_DATE_REV);
        final String formattedRevision = SimpleDateFormatUtil.getRevisionFormat().format(revision);
        return RevisionSourceIdentifier.create((String) root.getStatementArgument(), formattedRevision);
    }
}
