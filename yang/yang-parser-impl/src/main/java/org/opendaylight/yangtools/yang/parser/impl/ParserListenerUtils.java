/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Argument_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Base_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Default_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Identityref_specificationContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Key_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.StringContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Type_body_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Units_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yin_element_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yin_element_stmtContext;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParserListenerUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ParserListenerUtils.class);
    private static final Splitter KEYDEF_SPLITTER = Splitter.on(' ').omitEmptyStrings();
    private static final CharMatcher DOUBLE_QUOTE_MATCHER = CharMatcher.is('"');
    private static final CharMatcher SINGLE_QUOTE_MATCHER = CharMatcher.is('\'');

    private ParserListenerUtils() {
    }

    /**
     * Parse given tree and get first string value.
     *
     * @param treeNode
     *            tree to parse
     * @return first string value from given tree
     */
    public static String stringFromNode(final ParseTree treeNode) {
        String result = "";
        for (int i = 0; i < treeNode.getChildCount(); ++i) {
            final ParseTree child = treeNode.getChild(i);
            if (child instanceof StringContext) {
                return stringFromStringContext((StringContext)child);
            }
        }
        return result;
    }

    private static String stringFromStringContext(final StringContext context) {
        StringBuilder sb = new StringBuilder();
        for (TerminalNode stringNode : context.STRING()) {
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
            /*
             *
             * It is safe not to check last argument to be same
             * grammars enforces that.
             *
             * FIXME: Introduce proper escaping and translation of escaped
             * characters here.
             *
             */
            sb.append(quoteMatcher.removeFrom(str.substring(1, str.length()-1)));
        }
        return sb.toString();
    }

    /**
     * Parse given context and return its value;
     *
     * @param ctx
     *            status context
     * @return value parsed from context
     */
    public static Status parseStatus(final Status_stmtContext ctx) {
        Status result = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree statusArg = ctx.getChild(i);
            if (statusArg instanceof Status_argContext) {
                String statusArgStr = stringFromNode(statusArg);
                switch (statusArgStr) {
                case "current":
                    result = Status.CURRENT;
                    break;
                case "deprecated":
                    result = Status.DEPRECATED;
                    break;
                case "obsolete":
                    result = Status.OBSOLETE;
                    break;
                default:
                    LOG.warn("Invalid 'status' statement: " + statusArgStr);
                }
            }
        }
        return result;
    }

    /**
     * Parse given tree and returns units statement as string.
     *
     * @param ctx
     *            context to parse
     * @return value of units statement as string or null if there is no units
     *         statement
     */
    public static String parseUnits(final ParseTree ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Units_stmtContext) {
                return stringFromNode(child);
            }
        }
        return null;
    }

    /**
     * Parse given tree and returns default statement as string.
     *
     * @param ctx
     *            context to parse
     * @return value of default statement as string or null if there is no
     *         default statement
     */
    public static String parseDefault(final ParseTree ctx) {
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                return stringFromNode(child);
            }
        }
        return null;
    }

    /**
     * Create java.util.LinkedHashSet of key node names.
     *
     * @param ctx
     *            Key_stmtContext context
     * @return YANG list key as java.util.LinkedHashSet of key node names
     */
    public static Set<String> createListKey(final Key_stmtContext ctx) {
        final String keyDefinition = stringFromNode(ctx);
        return Sets.newLinkedHashSet(KEYDEF_SPLITTER.split(keyDefinition));
    }

    /**
     * Parse 'ordered-by' statement.
     *
     * The 'ordered-by' statement defines whether the order of entries within a
     * list are determined by the user or the system. The argument is one of the
     * strings "system" or "user". If not present, order defaults to "system".
     *
     * @param ctx
     *            Ordered_by_stmtContext
     * @return true, if ordered-by contains value 'user', false otherwise
     */
    public static boolean parseUserOrdered(final Ordered_by_stmtContext ctx) {
        boolean result = false;
        for (int j = 0; j < ctx.getChildCount(); j++) {
            ParseTree orderArg = ctx.getChild(j);
            if (orderArg instanceof Ordered_by_argContext) {
                String orderStr = stringFromNode(orderArg);
                switch (orderStr) {
                case "system":
                    result = false;
                    break;
                case "user":
                    result = true;
                    break;
                default:
                    LOG.warn("Invalid 'ordered-by' statement.");
                }
            }
        }
        return result;
    }

    /**
     * Parse given context and find identityref base value.
     *
     * @param ctx
     *            type body
     * @return identityref base value as String
     */
    public static String getIdentityrefBase(final Type_body_stmtsContext ctx) {
        String result = null;
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child instanceof Identityref_specificationContext) {
                for (int j = 0; j < child.getChildCount(); j++) {
                    ParseTree baseArg = child.getChild(j);
                    if (baseArg instanceof Base_stmtContext) {
                        result = stringFromNode(baseArg);
                        break outer;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Parse given context and return yin value.
     *
     * @param ctx
     *            context to parse
     * @return true if value is 'true', false otherwise
     */
    public static boolean parseYinValue(final Argument_stmtContext ctx) {
        boolean yinValue = false;
        outer: for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree yin = ctx.getChild(i);
            if (yin instanceof Yin_element_stmtContext) {
                for (int j = 0; j < yin.getChildCount(); j++) {
                    ParseTree yinArg = yin.getChild(j);
                    if (yinArg instanceof Yin_element_argContext) {
                        String yinString = stringFromNode(yinArg);
                        if ("true".equals(yinString)) {
                            yinValue = true;
                            break outer;
                        }
                    }
                }
            }
        }
        return yinValue;
    }

    /**
     * Check this base type.
     *
     * @param typeName
     *            base YANG type name
     * @param moduleName
     *            name of current module
     * @param line
     *            line in module
     * @throws YangParseException
     *             if this is one of YANG type which MUST contain additional
     *             informations in its body
     */
    public static void checkMissingBody(final String typeName, final String moduleName, final int line) {
        switch (typeName) {
        case "decimal64":
            throw new YangParseException(moduleName, line,
                    "The 'fraction-digits' statement MUST be present if the type is 'decimal64'.");
        case "identityref":
            throw new YangParseException(moduleName, line,
                    "The 'base' statement MUST be present if the type is 'identityref'.");
        case "leafref":
            throw new YangParseException(moduleName, line,
                    "The 'path' statement MUST be present if the type is 'leafref'.");
        case "bits":
            throw new YangParseException(moduleName, line, "The 'bit' statement MUST be present if the type is 'bits'.");
        case "enumeration":
            throw new YangParseException(moduleName, line,
                    "The 'enum' statement MUST be present if the type is 'enumeration'.");
        }
    }

    public static String getArgumentString(final org.antlr.v4.runtime.ParserRuleContext ctx) {
        List<StringContext> potentialValues = ctx.getRuleContexts(StringContext.class);
        checkState(!potentialValues.isEmpty());
        return ParserListenerUtils.stringFromStringContext(potentialValues.get(0));
    }

    public static <T extends ParserRuleContext> Optional<T> getFirstContext(final ParserRuleContext context,final Class<T> contextType) {
        List<T> potential = context.getRuleContexts(contextType);
        if (potential.isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(potential.get(0));
    }

}
