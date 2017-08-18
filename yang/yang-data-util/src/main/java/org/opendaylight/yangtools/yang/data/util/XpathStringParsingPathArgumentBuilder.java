/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Iterator which lazily parses {@link PathArgument} from string representation.
 *
 * <p>
 * Note that invocation of {@link #hasNext()} or {@link #next()} may result in
 * throwing of {@link IllegalArgumentException} if underlying string representation
 * is not correctly serialized or does not represent instance identifier valid
 * for associated schema context.
 */
class XpathStringParsingPathArgumentBuilder implements Builder<Collection<PathArgument>> {

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

    private static final CharMatcher QUOTE = CharMatcher.anyOf("'\"");

    private static final char SLASH = '/';
    private static final char COLON = ':';
    private static final char DOT = '.';
    private static final char EQUALS = '=';
    private static final char PRECONDITION_START = '[';
    private static final char PRECONDITION_END = ']';

    private final AbstractStringInstanceIdentifierCodec codec;
    private final String data;

    private final List<PathArgument> product = new ArrayList<>();

    private DataSchemaContextNode<?> current;
    private int offset;

    XpathStringParsingPathArgumentBuilder(final AbstractStringInstanceIdentifierCodec codec, final String data) {
        this.codec = requireNonNull(codec);
        this.data = requireNonNull(data);
        this.current = codec.getDataContextTree().getRoot();
        this.offset = 0;
    }

    @Override
    public Collection<PathArgument> build() {
        while (!allCharactersConsumed()) {
            product.add(computeNextArgument());
        }
        return ImmutableList.copyOf(product);
    }

    private PathArgument computeNextArgument() {
        checkValid(SLASH == currentChar(), "Identifier must start with '/'.");
        skipCurrentChar();
        checkValid(!allCharactersConsumed(), "Identifier cannot end with '/'.");
        QName name = nextQName();
        if (allCharactersConsumed() || SLASH == currentChar()) {
            return computeIdentifier(name);
        }

        checkValid(PRECONDITION_START == currentChar(), "Last element must be identifier, predicate or '/'");
        return computeIdentifierWithPredicate(name);
    }

    private DataSchemaContextNode<?> nextContextNode(final QName name) {
        current = current.getChild(name);
        checkValid(current != null, "%s is not correct schema node identifier.",name);
        while (current.isMixin()) {
            product.add(current.getIdentifier());
            current = current.getChild(name);
        }
        return current;
    }

    /**
     * Creates path argument with predicates and sets offset
     * to end of path argument.
     *
     * {@code
     *     predicate = "[" *WSP (predicate-expr / pos) *WSP "]"
     *     predicate-expr = (node-identifier / ".") *WSP "=" *WSP
     *          ((DQUOTE string DQUOTE) /
     *           (SQUOTE string SQUOTE))
     *     pos = non-negative-integer-value
     * }
     *
     * @param name QName of node, for which predicates are computed.
     * @return PathArgument representing node selection with predictes
     */
    private PathArgument computeIdentifierWithPredicate(final QName name) {
        DataSchemaContextNode<?> currentNode = nextContextNode(name);
        checkValid(currentNode.isKeyedEntry(), "Entry %s does not allow specifying predicates.", name);

        ImmutableMap.Builder<QName,Object> keyValues = ImmutableMap.builder();
        while (!allCharactersConsumed() && PRECONDITION_START == currentChar()) {
            skipCurrentChar();
            skipWhitespaces();
            final QName key;
            if (DOT == currentChar()) {
                key = null;
                skipCurrentChar();
            } else {
                key = nextQName();
            }
            skipWhitespaces();
            checkCurrentAndSkip(EQUALS, "Precondition must contain '='");
            skipWhitespaces();
            final String keyValue = nextQuotedValue();
            skipWhitespaces();
            checkCurrentAndSkip(PRECONDITION_END, "Precondition must ends with ']'");

            // Break-out from method for leaf-list case
            if (key == null && currentNode.isLeaf()) {
                checkValid(offset == data.length(), "Leaf argument must be last argument of instance identifier.");
                return new NodeWithValue<>(name, keyValue);
            }
            final DataSchemaContextNode<?> keyNode = currentNode.getChild(key);
            checkValid(keyNode != null, "%s is not correct schema node identifier.", key);
            final Object value = codec.deserializeKeyValue(keyNode.getDataSchemaNode(), keyValue);
            keyValues.put(key, value);
        }
        return new NodeIdentifierWithPredicates(name, keyValues.build());
    }


    private PathArgument computeIdentifier(final QName name) {
        DataSchemaContextNode<?> currentNode = nextContextNode(name);
        checkValid(!currentNode.isKeyedEntry(), "Entry %s requires key or value predicate to be present", name);
        return currentNode.getIdentifier();
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
        if (COLON == currentChar()) {
            // previous token is prefix;
            prefix = maybePrefix;
            skipCurrentChar();
            localName = nextIdentifier();
        } else {
            prefix = "";
            localName = maybePrefix;
        }
        return codec.createQName(prefix, localName);
    }

    /**
     * Returns true if all characters from input string were consumed.
     *
     * @return true if all characters from input string were consumed.
     */
    private boolean allCharactersConsumed() {
        return offset == data.length();
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
     * Fails parsing if a condition is not met.
     *
     * <p>
     * In case of error provides pointer to failed instance identifier,
     * offset on which failure occurred with explanation.
     *
     * @param condition Fails parsing if {@code condition} is false
     * @param errorMsg Error message which will be provided to user.
     */
    private void checkValid(final boolean condition, final String errorMsg, final Object... attributes) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(
                "Could not parse Instance Identifier '%s'. Offset: %s : Reason: %s", data, offset,
                String.format(errorMsg, attributes)));
        }
    }

    /**
     * Returns following value of quoted literal (without quotes) and sets offset after literal.
     *
     * @return String literal
     */
    private String nextQuotedValue() {
        final char quoteChar = currentChar();
        checkValid(QUOTE.matches(quoteChar), "Value must be qoute escaped with ''' or '\"'.");
        skipCurrentChar();
        final int valueStart = offset;
        final int endQoute = data.indexOf(quoteChar, offset);
        final String value = data.substring(valueStart, endQoute);
        offset = endQoute;
        skipCurrentChar();
        return value;
    }

    /**
     * Returns character at current offset.
     *
     * @return character at current offset.
     */
    private char currentChar() {
        return data.charAt(offset);
    }

    /**
     * Increases processing offset by 1.
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
     * Returns string which matches IDENTIFIER YANG ABNF token
     * and sets processing offset after end of identifier.
     *
     * @return string which matches IDENTIFIER YANG ABNF token
     */
    private String nextIdentifier() {
        checkValid(IDENTIFIER_FIRST_CHAR.matches(currentChar()),
            "Identifier must start with character from set 'a-zA-Z_'");
        final int start = offset;
        nextSequenceEnd(IDENTIFIER);
        return data.substring(start, offset);
    }

    private void nextSequenceEnd(final CharMatcher matcher) {
        while (!allCharactersConsumed() && matcher.matches(data.charAt(offset))) {
            offset++;
        }
    }
}
