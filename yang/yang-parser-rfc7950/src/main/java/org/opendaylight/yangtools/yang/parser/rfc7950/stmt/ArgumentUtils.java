/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static org.opendaylight.yangtools.yang.common.YangConstants.YANG_XPATH_FUNCTIONS_PREFIX;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpressionException;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
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

    @Regex
    private static final String YANG_XPATH_FUNCTIONS_STRING =
            "(re-match|deref|derived-from(-or-self)?|enum-value|bit-is-set)([ \t\r\n]*)(\\()";
    private static final Pattern YANG_XPATH_FUNCTIONS_PATTERN = Pattern.compile(YANG_XPATH_FUNCTIONS_STRING);

    @Regex
    private static final String PATH_ABS_STR = "/[^/].*";
    private static final Pattern PATH_ABS = Pattern.compile(PATH_ABS_STR);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();

    private static final YangXPathParserFactory XPATH_FACTORY;

    static {
        final Iterator<YangXPathParserFactory> it = ServiceLoader.load(YangXPathParserFactory.class).iterator();
        try {
            XPATH_FACTORY = it.next();
        } catch (NoSuchElementException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
            final StatementDefinition def = ctx.getPublicDefinition();
            throw new SourceException(ctx.getStatementSourceReference(),
                "Invalid '%s' statement %s '%s', it can be either 'true' or 'false'",
                def.getStatementName(), def.getArgumentDefinition().get().getArgumentName(), input);
        }
    }

    public static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: provide a proper namespace context
        //        xPath.setNamespaceContext(StmtNamespaceContext.create(ctx,
        //            ImmutableBiMap.of(RFC6020_YANG_NAMESPACE_STRING, YANG_XPATH_FUNCTIONS_PREFIX)));
        final YangXPathParser.QualifiedBound parser = XPATH_FACTORY.newParser(new YangNamespaceContext() {
            @Override
            public @NonNull Optional<String> findPrefixForNamespace(QNameModule namespace) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public @NonNull Optional<QNameModule> findNamespaceForPrefix(String prefix) {
                // TODO Auto-generated method stub
                return null;
            }
        });

        final QualifiedBound parsed;
        try {
            parsed = parser.parseExpression(path);
        } catch (XPathExpressionException e) {
            LOG.warn("Argument \"{}\" is not valid XPath string at \"{}\"", path, ctx.getStatementSourceReference(), e);
            return new RevisionAwareXPathImpl(path, isAbsoluteXPath(path));
        }

        return new RevisionAwareXPath.WithExpression() {

            @Override
            public boolean isAbsolute() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public String getOriginalString() {
                return path;
            }

            @Override
            public QualifiedBound getXPathExpression() {
                return parsed;
            }
        };
    }

    public static boolean isAbsoluteXPath(final String path) {
        return PATH_ABS.matcher(path).matches();
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
            final StringBuilder result = new StringBuilder();
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
