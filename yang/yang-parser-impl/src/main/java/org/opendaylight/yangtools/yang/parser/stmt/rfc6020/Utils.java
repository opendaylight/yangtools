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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StmtContextUtils;
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
                    StmtContextUtils.qnameFromArgument(ctx, keyToken));
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
                final QName qName = StmtContextUtils.qnameFromArgument(ctx, nodeName);
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

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(final StmtContext<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return (StatementContextBase<?, ?, ?>) rootStmtCtx.getFromNamespace(SchemaNodeIdentifierBuildNamespace.class, node);
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

    public static boolean isInExtensionBody(final StmtContext<?, ?, ?> stmtCtx) {
        StmtContext<?, ?, ?> current = stmtCtx;
        while (current.getParentContext().getParentContext() != null) {
            current = current.getParentContext();
            if (StmtContextUtils.producesDeclared(current, UnknownStatementImpl.class)) {
                return true;
            }
        }
    
        return false;
    }

    /**
     * Checks if the statement context has a 'yang-data' extension node as its parent.
     *
     * @param stmtCtx statement context to be checked
     * @return true if the parent node is a 'yang-data' node, otherwise false
     */
    public static boolean hasYangDataExtensionParent(final StmtContext<?, ?, ?> stmtCtx) {
        return StmtContextUtils.producesDeclared(stmtCtx.getParentContext(), YangDataStatementImpl.class);
    }

    public static boolean isUnknownStatement(final StmtContext<?, ?, ?> stmtCtx) {
        return StmtContextUtils.producesDeclared(stmtCtx, UnknownStatementImpl.class);
    }
}
