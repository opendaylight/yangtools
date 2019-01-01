/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_NAMESPACE_STRING;
import static org.opendaylight.yangtools.yang.common.YangConstants.YANG_XPATH_FUNCTIONS_PREFIX;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableBiMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for dealing with arguments encountered by StatementSupport classes. Note that using this class may
 * result in thread-local state getting attached. To clean up this state, please invoke
 * {@link #detachFromCurrentThread()} when appropriate.
 */
@Beta
public final class ArgumentUtils {
    public static final Splitter PIPE_SPLITTER = Splitter.on('|').trimResults();
    public static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    private static final Logger LOG = LoggerFactory.getLogger(ArgumentUtils.class);

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

    // these objects are to compare whether range has MAX or MIN value
    // none of these values should appear as Yang number according to spec so they are safe to use
    private static final BigDecimal YANG_MIN_NUM = BigDecimal.valueOf(-Double.MAX_VALUE);
    private static final BigDecimal YANG_MAX_NUM = BigDecimal.valueOf(Double.MAX_VALUE);

    private ArgumentUtils() {
        throw new UnsupportedOperationException();
    }

    public static int compareNumbers(final Number n1, final Number n2) {
        final BigDecimal num1 = yangConstraintToBigDecimal(n1);
        final BigDecimal num2 = yangConstraintToBigDecimal(n2);
        return new BigDecimal(num1.toString()).compareTo(new BigDecimal(num2.toString()));
    }

    public static String internBoolean(final String input) {
        if ("true".equals(input)) {
            return "true";
        } else if ("false".equals(input)) {
            return "false";
        } else {
            return input;
        }
    }

    public static @NonNull Boolean parseBoolean(final StmtContext<?, ?, ?> ctx, final String input) {
        if ("true".equals(input)) {
            return Boolean.TRUE;
        } else if ("false".equals(input)) {
            return Boolean.FALSE;
        } else {
            throw new SourceException(ctx.getStatementSourceReference(),
                "Invalid '%s' statement %s '%s', it can be either 'true' or 'false'",
                ctx.getPublicDefinition().getStatementName(), ctx.getPublicDefinition().getArgumentName(), input);
        }
    }

    public static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
        final XPath xPath = XPATH_FACTORY.get().newXPath();
        xPath.setNamespaceContext(StmtNamespaceContext.create(ctx,
                ImmutableBiMap.of(RFC6020_YANG_NAMESPACE_STRING, YANG_XPATH_FUNCTIONS_PREFIX)));

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
                qNames.add(StmtContextUtils.parseNodeIdentifier(ctx, nodeName));
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

    private static BigDecimal yangConstraintToBigDecimal(final Number number) {
        if (UnresolvedNumber.max().equals(number)) {
            return YANG_MAX_NUM;
        }
        if (UnresolvedNumber.min().equals(number)) {
            return YANG_MIN_NUM;
        }

        return new BigDecimal(number.toString());
    }
}
