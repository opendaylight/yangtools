/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

@Beta
final class LeafrefXPathStringParsingPathArgumentBuilder implements Builder<List<PathArgument>> {

    private static final String UP_ONE_LEVEL = "..";
    private static final String CURRENT_FUNCTION_INVOCATION_STR = "current()";

    @RegEx
    private static final String NODE_IDENTIFIER_STR = "([A-Za-z_][A-Za-z0-9_\\.-]*:)?([A-Za-z_][A-Za-z0-9_\\.-]*)";

    /**
     * Pattern matching node-identifier YANG ABNF token.
     */
    private static final Pattern NODE_IDENTIFIER_PATTERN = Pattern.compile(NODE_IDENTIFIER_STR);

    /**
     * Matcher matching WSP YANG ABNF token.
     */
    private static final CharMatcher WSP = CharMatcher.anyOf(" \t");

    /**
     * Matcher matching IDENTIFIER first char token.
     */
    private static final CharMatcher IDENTIFIER_FIRST_CHAR = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.is('_')).precomputed();
    /**
     * Matcher matching IDENTIFIER token.
     */
    private static final CharMatcher IDENTIFIER = IDENTIFIER_FIRST_CHAR.or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.anyOf(".-")).precomputed();

    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private static final char SLASH = '/';
    private static final char COLON = ':';
    private static final char EQUALS = '=';
    private static final char PRECONDITION_START = '[';
    private static final char PRECONDITION_END = ']';

    private final String xpathString;
    private final SchemaContext schemaContext;
    private final TypedSchemaNode schemaNode;
    private final NormalizedNodeContext currentNodeCtx;
    private final List<PathArgument> product = new ArrayList<>();

    private int offset = 0;

    LeafrefXPathStringParsingPathArgumentBuilder(final String xpathString, final SchemaContext schemaContext,
            final TypedSchemaNode schemaNode, final NormalizedNodeContext currentNodeCtx) {
        this.xpathString = xpathString;
        this.schemaContext = schemaContext;
        this.schemaNode = schemaNode;
        this.currentNodeCtx = currentNodeCtx;
    }

    @Override
    public List<PathArgument> build() {
        while (!allCharactersConsumed()) {
            product.add(computeNextArgument());
        }
        return ImmutableList.copyOf(product);
    }

    private PathArgument computeNextArgument() {
        checkValid(SLASH == currentChar(), "Identifier must start with '/'.");
        skipCurrentChar();
        checkValid(!allCharactersConsumed(), "Identifier cannot end with '/'.");

        final QName name = nextQName();
        if (allCharactersConsumed() || SLASH == currentChar()) {
            return new NodeIdentifier(name);
        }

        checkValid(PRECONDITION_START == currentChar(), "Last element must be identifier, predicate or '/'");
        return computeIdentifierWithPredicate(name);
    }

    private PathArgument computeIdentifierWithPredicate(final QName name) {
        product.add(new NodeIdentifier(name));

        ImmutableMap.Builder<QName, Object> keyValues = ImmutableMap.builder();
        while (!allCharactersConsumed() && PRECONDITION_START == currentChar()) {
            skipCurrentChar();
            skipWhitespaces();
            final QName key = nextQName();

            skipWhitespaces();
            checkCurrentAndSkip(EQUALS, "Precondition must contain '='");
            skipWhitespaces();
            final Object keyValue = nextCurrentFunctionPathValue();
            skipWhitespaces();
            checkCurrentAndSkip(PRECONDITION_END, "Precondition must ends with ']'");

            keyValues.put(key, keyValue);
        }
        return new NodeIdentifierWithPredicates(name, keyValues.build());
    }

    private Object nextCurrentFunctionPathValue() {
        final String xPathSubStr = xpathString.substring(offset);
        final String pathKeyExpression = xPathSubStr.substring(0, xPathSubStr.indexOf(PRECONDITION_END));
        final String relPathKeyExpression = pathKeyExpression.substring(CURRENT_FUNCTION_INVOCATION_STR.length());

        offset += CURRENT_FUNCTION_INVOCATION_STR.length();
        skipWhitespaces();
        checkCurrentAndSkip(SLASH, "Expression 'current()' must be followed by slash.");
        skipWhitespaces();

        final List<String> pathComponents = SLASH_SPLITTER.trimResults().omitEmptyStrings()
                .splitToList(relPathKeyExpression);
        checkValid(!pathComponents.isEmpty(), "Malformed path key expression: '%s'.", pathKeyExpression);

        boolean inNodeIdentifierPart = false;
        NormalizedNodeContext currentNodeCtx = this.currentNodeCtx;
        NormalizedNode<?, ?> currentNode = null;
        for (String pathComponent : pathComponents) {
            final Matcher matcher = NODE_IDENTIFIER_PATTERN.matcher(pathComponent);
            if (UP_ONE_LEVEL.equals(pathComponent)) {
                checkValid(!inNodeIdentifierPart, "Up-one-level expression cannot follow concrete path component.");
                currentNodeCtx = currentNodeCtx.getParent();
                currentNode = currentNodeCtx.getNode();
                offset += UP_ONE_LEVEL.length() + 1;
            } else if (matcher.matches()) {
                inNodeIdentifierPart = true;
                if (currentNode != null && currentNode instanceof DataContainerNode) {
                    final DataContainerNode dcn = (DataContainerNode) currentNode;
                    final Optional<NormalizedNode<?, ?>> possibleChild = dcn.getChild(new NodeIdentifier(nextQName()));
                    currentNode = possibleChild.isPresent() ? possibleChild.get() : null;
                }
            } else {
                throw new IllegalArgumentException(String.format(
                        "Could not parse leafref path '%s'. Offset: %s : Reason: Malformed path component: '%s'.",
                        xpathString, offset, pathComponent));
            }
        }

        if (currentNode != null && currentNode instanceof LeafNode) {
            return currentNode.getValue();
        }

        throw new IllegalArgumentException("Could not resolve current function path value.");

    }

    /**
     * Returns following QName and sets offset to end of QName.
     *
     * @return following QName.
     */
    private QName nextQName() {
        // Consume prefix or identifier
        final String maybePrefix = nextIdentifier();
        final String prefix;
        final String localName;
        if (!allCharactersConsumed() && COLON == currentChar()) {
            // previous token is prefix;
            prefix = maybePrefix;
            skipCurrentChar();
            localName = nextIdentifier();
        } else {
            prefix = "";
            localName = maybePrefix;
        }
        return createQName(prefix, localName);
    }

    /**
     * Returns true if all characters from input string were consumed.
     *
     * @return true if all characters from input string were consumed.
     */
    private boolean allCharactersConsumed() {
        return offset == xpathString.length();
    }

    private QName createQName(final String prefix, final String localName) {
        final Module module = schemaContext.findModuleByNamespaceAndRevision(schemaNode.getQName().getNamespace(),
                schemaNode.getQName().getRevision());
        if (prefix.isEmpty() || module.getPrefix().equals(prefix)) {
            return QName.create(module.getQNameModule(), localName);
        }

        for (final ModuleImport moduleImport : module.getImports()) {
            if (prefix.equals(moduleImport.getPrefix())) {
                final Module importedModule = schemaContext.findModuleByName(moduleImport.getModuleName(),
                        moduleImport.getRevision());
                return QName.create(importedModule.getQNameModule(),localName);
            }
        }

        throw new IllegalArgumentException(String.format("Failed to lookup a module for prefix %s", prefix));
    }

    /**
     * Skips current char if it equals expected otherwise fails parsing.
     *
     * @param expected Expected character
     * @param errorMsg Error message if {@link #currentChar()} does not match expected.
     */
    private void checkCurrentAndSkip(final char expected, final String errorMsg) {
        checkValid(expected == currentChar(), errorMsg);
        offset++;
    }

    /**
     * Fails parsing if condition is not met.
     *
     * <p>
     * In case of error provides pointer to failed leafref, offset on which failure occured with explanation.
     *
     * @param condition Fails parsing if {@code condition} is false
     * @param errorMsg Error message which will be provided to user.
     * @param attributes Message attributes
     */
    private void checkValid(final boolean condition, final String errorMsg, final Object... attributes) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(
                    "Could not parse leafref path '%s'. Offset: %s : Reason: %s", xpathString, offset,
                    String.format(errorMsg, attributes)));
        }
    }

    /**
     * Returns character at current offset.
     *
     * @return character at current offset.
     */
    private char currentChar() {
        return xpathString.charAt(offset);
    }

    /**
     * Increments processing offset by 1.
     */
    private void skipCurrentChar() {
        offset++;
    }

    /**
     * Skip whitespace characters, sets offset to first following non-whitespace character.
     */
    private void skipWhitespaces() {
        nextSequenceEnd(WSP);
    }

    /**
     * Returns a string which matches IDENTIFIER YANG ABNF token
     * and sets processing offset after the end of identifier.
     *
     * @return string which matches IDENTIFIER YANG ABNF token
     */
    private String nextIdentifier() {
        int start = offset;
        checkValid(IDENTIFIER_FIRST_CHAR.matches(currentChar()),
                "Identifier must start with character from set 'a-zA-Z_'");
        nextSequenceEnd(IDENTIFIER);
        return xpathString.substring(start, offset);
    }

    private void nextSequenceEnd(final CharMatcher matcher) {
        while (!allCharactersConsumed() && matcher.matches(xpathString.charAt(offset))) {
            offset++;
        }
    }
}
