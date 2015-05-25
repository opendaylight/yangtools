/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.LengthConstraintEffectiveImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.RangeConstraintEffectiveImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public final class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher
            .is('\'');

    private static final Splitter PIPE_SPLITTER = Splitter.on("|")
            .trimResults();
    private static final Splitter TWO_DOTS_SPLITTER = Splitter.on("..")
            .trimResults();
    public static final QName EMPTY_QNAME = QName.create("empty", "empty");

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

    private static final Set<String> STATEMENT_BUILD_IN_TYPES = ImmutableSet
            .<String> builder().add(BOOLEAN).add(DECIMAL64).add(EMPTY)
            .add(INT8).add(INT16).add(INT32).add(INT64).add(STRING).add(UINT8)
            .add(UINT16).add(UINT32).add(UINT64).build();

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
                if (prefixes != null
                        && prefixes.get(prefix) != null
                        && stmtDef
                                .get(new QName(
                                        YangConstants.RFC6020_YIN_NAMESPACE,
                                        localName)) != null) {
                    return true;
                } else {
                    if (stmtDef.get(new QName(
                            YangConstants.RFC6020_YIN_NAMESPACE, localName)) != null) {
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

        if (value == null || value.equals("")) {
            return EMPTY_QNAME;
        }

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

        QNameModule resultQNameModule = qNameModule.getRevision() == null ? QNameModule
                .create(qNameModule.getNamespace(),
                        SimpleDateFormatUtil.DEFAULT_DATE_REV) : qNameModule;

        return QName.create(resultQNameModule, localName);
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(
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
    public static StatementContextBase<?, ?, ?> findNode(
            StatementContextBase<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return findNode(rootStmtCtx, node.getPathFromRoot());
    }

    public static SchemaPath getSchemaPath(StmtContext<?, ?, ?> ctx) {

        Iterator<StmtContext<?, ?, ?>> iteratorFromRoot = ctx
                .getStmtContextsFromRoot().iterator();

        if (iteratorFromRoot.hasNext()) {
            iteratorFromRoot.next(); // skip root argument
        }

        List<QName> qNamesFromRoot = new LinkedList<>();
        while (iteratorFromRoot.hasNext()) {
            StmtContext<?, ?, ?> nextStmtCtx = iteratorFromRoot.next();
            Object nextStmtArgument = nextStmtCtx.getStatementArgument();
            if (nextStmtArgument instanceof QName) {
                QName qname = (QName) nextStmtArgument;
                qNamesFromRoot.add(qname);
            } else if (StmtContextUtils.producesDeclared(nextStmtCtx,
                    AugmentStatement.class)
                    && nextStmtArgument instanceof SchemaNodeIdentifier) {
                addQNamesFromSchemaNodeIdentifierToList(qNamesFromRoot,
                        (SchemaNodeIdentifier) nextStmtArgument);
            } else {
                return SchemaPath.SAME;
            }
        }

        return SchemaPath.create(qNamesFromRoot, true);
    }

    private static void addQNamesFromSchemaNodeIdentifierToList(
            List<QName> qNamesFromRoot, SchemaNodeIdentifier augmentTargetPath) {
        Iterator<QName> augmentTargetPathIterator = augmentTargetPath
                .getPathFromRoot().iterator();
        while (augmentTargetPathIterator.hasNext()) {
            qNamesFromRoot.add(augmentTargetPathIterator.next());
        }
    }

    public static Deviation.Deviate parseDeviateFromString(final String deviate) {

        // Yang constants should be lowercase so we have throw if value does not suit this
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

    public static SchemaPath SchemaNodeIdentifierToSchemaPath(
            SchemaNodeIdentifier identifier) {
        return SchemaPath.create(identifier.getPathFromRoot(),
                identifier.isAbsolute());
    }

    public static boolean isYangStatementBuildInType(final String type) {
        return STATEMENT_BUILD_IN_TYPES.contains(type);
    }

    private static BigInteger parseIntegerConstraintValue(final String value) {
        BigInteger result;

        if ("min".equals(value)) {
            result = RangeStatementImpl.YANG_MIN_NUM.toBigInteger();
        } else if ("max".equals(value)) {
            result = RangeStatementImpl.YANG_MAX_NUM.toBigInteger();
        } else {
            try {
                result = new BigInteger(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format(
                        "Value %s is not a valid integer", value), e);
            }
        }
        return result;
    }

    private static BigDecimal parseDecimalConstraintValue(final String value) {
        BigDecimal result;

        if ("min".equals(value)) {
            result = RangeStatementImpl.YANG_MIN_NUM;
        } else if ("max".equals(value)) {
            result = RangeStatementImpl.YANG_MAX_NUM;
        } else {
            try {
                result = new BigDecimal(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format(
                        "Value %s is not a valid decimal number", value), e);
            }
        }
        return result;
    }

    public static List<RangeConstraint> parseRangeListFromString(
            String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<RangeConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(
                    singleRange).iterator();
            final BigDecimal min = parseDecimalConstraintValue(boundaries
                    .next());

            final BigDecimal max;
            if (boundaries.hasNext()) {
                max = parseDecimalConstraintValue(boundaries.next());

                // if min larger than max then error
                if (min.compareTo(max) == 1) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Range constraint %s has descending order of boundaries; should be ascending",
                                    singleRange));
                }
                if (boundaries.hasNext()) {
                    throw new IllegalArgumentException(
                            "Wrong number of boundaries in range constraint "
                                    + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && min.compareTo((BigDecimal) Iterables.getLast(
                            rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Some of the ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new RangeConstraintEffectiveImpl(min, max,
                    description, reference));
        }

        return rangeConstraints;
    }

    public static List<LengthConstraint> parseLengthListFromString(
            String rangeArgument) {

        Optional<String> description = Optional.absent();
        Optional<String> reference = Optional.absent();

        List<LengthConstraint> rangeConstraints = new ArrayList<>();

        for (final String singleRange : PIPE_SPLITTER.split(rangeArgument)) {
            final Iterator<String> boundaries = TWO_DOTS_SPLITTER.splitToList(
                    singleRange).iterator();
            final BigInteger min = parseIntegerConstraintValue(boundaries
                    .next());

            final BigInteger max;
            if (boundaries.hasNext()) {
                max = parseIntegerConstraintValue(boundaries.next());

                // if min larger than max then error
                if (min.compareTo(max) == 1) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Length constraint %s has descending order of boundaries; should be ascending",
                                    singleRange));
                }
                if (boundaries.hasNext()) {
                    throw new IllegalArgumentException(
                            "Wrong number of boundaries in length constraint "
                                    + singleRange);
                }
            } else {
                max = min;
            }

            // some of intervals overlapping
            if (rangeConstraints.size() > 1
                    && min.compareTo((BigInteger) Iterables.getLast(
                            rangeConstraints).getMax()) != 1) {
                throw new IllegalArgumentException(String.format(
                        "Some of the length ranges in %s are not disjoint",
                        rangeArgument));
            }

            rangeConstraints.add(new LengthConstraintEffectiveImpl(min, max,
                    description, reference));
        }

        return rangeConstraints;
    }

    public static StmtContext<?, TypedefStatement, EffectiveStatement<QName, TypedefStatement>> getBaseTypeFromCtx(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
        return ctx.getFromNamespace(TypeNamespace.class, qNameFromArgument(ctx, ctx.getStatementArgument()));
    }
}
