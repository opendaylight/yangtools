/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.Splitter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UnresolvedNumber;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Utility class for dealing with arguments encountered by StatementSupport classes.
 */
@Beta
public final class ArgumentUtils {
    public static final Splitter PIPE_SPLITTER = Splitter.on('|').trimResults();
    public static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..").trimResults();

    @Regex
    private static final String PATH_ABS_STR = "/[^/].*";
    private static final Pattern PATH_ABS = Pattern.compile(PATH_ABS_STR);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();

    // these objects are to compare whether range has MAX or MIN value
    // none of these values should appear as Yang number according to spec so they are safe to use
    private static final BigDecimal YANG_MIN_NUM = BigDecimal.valueOf(-Double.MAX_VALUE);
    private static final BigDecimal YANG_MAX_NUM = BigDecimal.valueOf(Double.MAX_VALUE);

    private ArgumentUtils() {
        // Hidden on purpose
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
        return XPathSupport.parseXPath(ctx, path);
    }

    public static boolean isAbsoluteXPath(final String path) {
        return PATH_ABS.matcher(path).matches();
    }

    public static Descendant parseDescendantSchemaNodeIdentifier(final StmtContext<?, ?, ?> ctx, final String str) {
        // FIXME: this does accept a leading slash
        return Descendant.of(parseNodeIdentifiers(ctx, str));
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public static SchemaNodeIdentifier nodeIdentifierFromPath(final StmtContext<?, ?, ?> ctx, final String path) {
        final List<QName> qnames = parseNodeIdentifiers(ctx, path);
        return PATH_ABS.matcher(path).matches() ? Absolute.of(qnames) : Descendant.of(qnames);
    }

    private static  List<QName> parseNodeIdentifiers(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: is the path trimming really necessary??
        final List<QName> qnames = new ArrayList<>();
        for (final String nodeName : SLASH_SPLITTER.split(trimSingleLastSlashFromXPath(path))) {
            try {
                qnames.add(StmtContextUtils.parseNodeIdentifier(ctx, nodeName));
            } catch (final RuntimeException e) {
                throw new SourceException(ctx.getStatementSourceReference(), e,
                        "Failed to parse node '%s' in path '%s'", nodeName, path);
            }
        }

        if (qnames.isEmpty()) {
            throw new SourceException("Schema node identifier must not be empty", ctx.getStatementSourceReference());
        }
        return qnames;
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
