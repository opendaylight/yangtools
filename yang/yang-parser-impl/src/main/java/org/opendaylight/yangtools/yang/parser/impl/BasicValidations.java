/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static java.lang.String.format;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yang_version_stmtContext;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

/**
 * Reusable checks of basic constraints on yang statements
 */
final class BasicValidations {

    static final String SUPPORTED_YANG_VERSION = "1";
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.-]*");
    private static final Pattern PREFIX_IDENTIFIER_PATTERN = Pattern.compile("(.+):(.+)");

    /**
     * It isn't desirable to create instance of this class.
     */
    private BasicValidations() {
    }

    static void checkNotPresentBoth(final ParseTree parent, final Class<? extends ParseTree> childType1,
            final Class<? extends ParseTree> childType2) {
        if (BasicValidations.checkPresentChildOfTypeSafe(parent, childType1, true)
                && BasicValidations.checkPresentChildOfTypeSafe(parent, childType2, false)) {
            ValidationUtil.ex(ValidationUtil.f("(In (sub)module:%s) Both %s and %s statement present in %s:%s",
                    ValidationUtil.getRootParentName(parent), ValidationUtil.getSimpleStatementName(childType1),
                    ValidationUtil.getSimpleStatementName(childType2),
                    ValidationUtil.getSimpleStatementName(parent.getClass()), ValidationUtil.getName(parent)));
        }
    }

    static void checkOnlyPermittedValues(final ParseTree ctx, final Set<String> permittedValues) {
        String mandatory = ValidationUtil.getName(ctx);
        String rootParentName = ValidationUtil.getRootParentName(ctx);

        if (!permittedValues.contains(mandatory)) {
            ValidationUtil.ex(ValidationUtil.f(
                    "(In (sub)module:%s) %s:%s, illegal value for %s statement, only permitted:%s", rootParentName,
                    ValidationUtil.getSimpleStatementName(ctx.getClass()), mandatory,
                    ValidationUtil.getSimpleStatementName(ctx.getClass()), permittedValues));
        }
    }

    static void checkUniquenessInNamespace(final ParseTree stmt, final Set<String> uniques) {
        String name = ValidationUtil.getName(stmt);
        String rootParentName = ValidationUtil.getRootParentName(stmt);

        if (uniques.contains(name)) {
            ValidationUtil.ex(ValidationUtil.f("(In (sub)module:%s) %s:%s not unique in (sub)module", rootParentName,
                    ValidationUtil.getSimpleStatementName(stmt.getClass()), name));
        }
        uniques.add(name);
    }

    /**
     * Check if only one module or submodule is present in session(one yang
     * file)
     */
    static void checkIsModuleIdNull(final String globalId) {
        if (globalId != null) {
            ValidationUtil.ex(ValidationUtil.f("Multiple (sub)modules per file"));
        }
    }

    static void checkPresentYangVersion(final ParseTree ctx, final String moduleName) {
        if (!checkPresentChildOfTypeSafe(ctx, Yang_version_stmtContext.class, true)) {
            ValidationUtil.ex(ValidationUtil.f(
                    "Yang version statement not present in module:%s, Validating as yang version:%s", moduleName,
                    SUPPORTED_YANG_VERSION));
        }
    }

    static void checkDateFormat(final ParseTree stmt) {
        try {
            SimpleDateFormatUtil.getRevisionFormat().parse(ValidationUtil.getName(stmt));
        } catch (ParseException e) {
            String exceptionMessage = ValidationUtil.f(
                    "(In (sub)module:%s) %s:%s, invalid date format expected date format is:%s",
                    ValidationUtil.getRootParentName(stmt), ValidationUtil.getSimpleStatementName(stmt.getClass()),
                    ValidationUtil.getName(stmt), SimpleDateFormatUtil.getRevisionFormat().format(new Date()));
            ValidationUtil.ex(exceptionMessage);
        }
    }

    static void checkIdentifier(final ParseTree statement) {
        checkIdentifierInternal(statement, ValidationUtil.getName(statement));
    }

    static void checkIdentifierInternal(final ParseTree statement, final String name) {
        if (!IDENTIFIER_PATTERN.matcher(name).matches()) {

            String message = ValidationUtil.f("%s statement identifier:%s is not in required format:%s",
                    ValidationUtil.getSimpleStatementName(statement.getClass()), name, IDENTIFIER_PATTERN.toString());
            String parent = ValidationUtil.getRootParentName(statement);
            message = parent.equals(name) ? message : ValidationUtil.f("(In (sub)module:%s) %s", parent, message);

            if (statement instanceof ParserRuleContext) {
                message = "Error on line " + ((ParserRuleContext) statement).getStart().getLine() + ": " + message;
            }

            ValidationUtil.ex(message);
        }
    }

    static void checkPrefixedIdentifier(final ParseTree statement) {
        checkPrefixedIdentifierInternal(statement, ValidationUtil.getName(statement));
    }

    private static void checkPrefixedIdentifierInternal(final ParseTree statement, final String id) {
        Matcher matcher = PREFIX_IDENTIFIER_PATTERN.matcher(id);

        if (matcher.matches()) {
            try {
                // check prefix
                checkIdentifierInternal(statement, matcher.group(1));
                // check ID
                checkIdentifierInternal(statement, matcher.group(2));
            } catch (YangValidationException e) {
                ValidationUtil.ex(ValidationUtil.f("Prefixed id:%s not in required format, details:%s", id,
                        e.getMessage()));
            }
        } else {
            checkIdentifierInternal(statement, id);
        }
    }

    static void checkSchemaNodeIdentifier(final ParseTree statement) {
        String id = ValidationUtil.getName(statement);

        try {
            for (String oneOfId : SLASH_SPLITTER.split(id)) {
                checkPrefixedIdentifierInternal(statement, oneOfId);
            }
        } catch (YangValidationException e) {
            ValidationUtil.ex(ValidationUtil.f("Schema node id:%s not in required format, details:%s", id,
                    e.getMessage()));
        }
    }

    private interface MessageProvider {
        String getMessage();
    }

    private static void checkPresentChildOfTypeInternal(final ParseTree parent, final Set<Class<? extends ParseTree>> expectedChildType,
            final MessageProvider message, final boolean atMostOne) {
        if (!checkPresentChildOfTypeSafe(parent, expectedChildType, atMostOne)) {
            String str = atMostOne ? "(Expected exactly one statement) " + message.getMessage() : message.getMessage();
            ValidationUtil.ex(str);
        }
    }

    static void checkPresentChildOfType(final ParseTree parent, final Class<? extends ParseTree> expectedChildType,
            final boolean atMostOne) {

        // Construct message in checkPresentChildOfTypeInternal only if
        // validation fails, not in advance
        MessageProvider message = new MessageProvider() {

            @Override
            public String getMessage() {
                String message = ValidationUtil.f("Missing %s statement in %s:%s",
                        ValidationUtil.getSimpleStatementName(expectedChildType),
                        ValidationUtil.getSimpleStatementName(parent.getClass()), ValidationUtil.getName(parent));

                String root = ValidationUtil.getRootParentName(parent);
                message = format("(In (sub)module:%s) %s", root, message);
                return message;
            }
        };

        Set<Class<? extends ParseTree>> expectedChildTypeSet = Sets.newHashSet();
        expectedChildTypeSet.add(expectedChildType);

        checkPresentChildOfTypeInternal(parent, expectedChildTypeSet, message, atMostOne);
    }

    /**
     *
     * Implementation of interface <code>MessageProvider</code> for method
     * {@link BasicValidations#checkPresentChildOfTypeSafe(ParseTree, Set, boolean)
     * checkPresentChildOfTypeSafe(ParseTree, Set, boolean) * }
     */
    private static class MessageProviderForSetOfChildTypes implements MessageProvider {

        private final Set<Class<? extends ParseTree>> expectedChildTypes;
        private final ParseTree parent;

        public MessageProviderForSetOfChildTypes(final Set<Class<? extends ParseTree>> expectedChildTypes, final ParseTree parent) {
            this.expectedChildTypes = expectedChildTypes;
            this.parent = parent;
        }

        @Override
        public String getMessage() {
            StringBuilder childTypes = new StringBuilder();
            for (Class<? extends ParseTree> type : expectedChildTypes) {
                childTypes.append(ValidationUtil.getSimpleStatementName(type));
                childTypes.append(" OR ");
            }
            String message = ValidationUtil.f("Missing %s statement in %s:%s", childTypes.toString(),
                    ValidationUtil.getSimpleStatementName(parent.getClass()), ValidationUtil.getName(parent));

            String root = ValidationUtil.getRootParentName(parent);
            message = format("(In (sub)module:%s) %s", root, message);
            return message;
        }
    }

    static void checkPresentChildOfTypes(final ParseTree parent,
            final Set<Class<? extends ParseTree>> expectedChildTypes, final boolean atMostOne) {

        // Construct message in checkPresentChildOfTypeInternal only if
        // validation fails, not in advance
        MessageProvider message = new MessageProviderForSetOfChildTypes(expectedChildTypes, parent);
        checkPresentChildOfTypeInternal(parent, expectedChildTypes, message, atMostOne);
    }

    private static boolean checkPresentChildOfTypeSafe(final ParseTree parent, final Set<Class<? extends ParseTree>> expectedChildType,
            final boolean atMostOne) {

        int foundChildrenOfType = ValidationUtil.countPresentChildrenOfType(parent, expectedChildType);

        return atMostOne ? foundChildrenOfType == 1 : foundChildrenOfType != 0;
    }

    private static boolean checkPresentChildOfTypeSafe(final ParseTree parent, final Class<? extends ParseTree> expectedChildType,
            final boolean atMostOne) {

        int foundChildrenOfType = ValidationUtil.countPresentChildrenOfType(parent, expectedChildType);

        return atMostOne ? foundChildrenOfType == 1 : foundChildrenOfType != 0;
    }

    static Iterable<String> getAndCheckUniqueKeys(final ParseTree ctx) {
        String key = ValidationUtil.getName(ctx);
        ParseTree parent = ctx.getParent();
        String rootParentName = ValidationUtil.getRootParentName(ctx);

        Iterable<String> keyList = ValidationUtil.listKeysFromId(key);
        Set<String> duplicates = ValidationUtil.getDuplicates(keyList);

        if (!duplicates.isEmpty()) {
            ValidationUtil.ex(ValidationUtil.f("(In (sub)module:%s) %s:%s, %s:%s contains duplicates:%s",
                    rootParentName, ValidationUtil.getSimpleStatementName(parent.getClass()),
                    ValidationUtil.getName(parent), ValidationUtil.getSimpleStatementName(ctx.getClass()), key,
                    duplicates));
        }
        return keyList;
    }
}
