/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_NAMESPACE;
import static org.opendaylight.yangtools.yang.common.YangConstants.YANG_XPATH_FUNCTIONS_PREFIX;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableBiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class PathUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PathUtils.class);

    @RegEx
    private static final String YANG_XPATH_FUNCTIONS_STRING =
            "(re-match|deref|derived-from(-or-self)?|enum-value|bit-is-set)([ \t\r\n]*)(\\()";
    private static final Pattern YANG_XPATH_FUNCTIONS_PATTERN = Pattern.compile(YANG_XPATH_FUNCTIONS_STRING);

    @RegEx
    private static final String PATH_ABS_STR = "/[^/].*";
    private static final Pattern PATH_ABS = Pattern.compile(PATH_ABS_STR);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();

    // XPathFactory is not thread-safe, rather than locking around a shared instance, we use a thread-local one.
    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {
            return XPathFactory.newInstance();
        }
    };

    private PathUtils() {
        throw new UnsupportedOperationException();
    }

    public static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
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

    @SuppressWarnings("checkstyle:illegalCatch")
    public static SchemaNodeIdentifier nodeIdentifierFromPath(final StmtContext<?, ?, ?> ctx, final String path) {
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

    /**
     * Cleanup any resources attached to the current thread. Threads interacting with this class can cause thread-local
     * caches to them. Invoke this method if you want to detach those resources.
     */
    public static void detachFromCurrentThread() {
        XPATH_FACTORY.remove();
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

    private static String trimSingleLastSlashFromXPath(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
