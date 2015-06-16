/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
/**
 *
 * Iterator which lazily parses {@link PathArgument} from string representation.
 *
 * Note that invocation of {@link #hasNext()} or {@link #next()} may result in
 * throwing of {@link IllegalArgumentException} if underlying string represenation
 * is not correctly serialized or does not represent instance identifier valid
 * for associated schema context.
 *
 * In order to obtain {@link Iterable} or {@link java.util.Collection} please use
 * {@link com.google.common.collect.ImmutableList#copyOf(java.util.Iterator)}
 * with this Iterator, which will trigger computation of all path arguments.
 *
 */
class XpathStringParsingPathArgumentBuilder implements Builder<Iterable<PathArgument>> {

    /**
     * Matcher matching WSP YANG ABNF token
     *
     */
    private static final CharMatcher WSP = CharMatcher.anyOf(" \t");

    /**
     * Matcher matching IDENTIFIER first char token.
     *
     */
    private static final CharMatcher IDENTIFIER_FIRST_CHAR =
            CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z'))
            .or(CharMatcher.is('_')).precomputed();
    /**
     *
     * Matcher matching IDENTIFIER token
     *
     */
    private static final CharMatcher IDENTIFIER =
            IDENTIFIER_FIRST_CHAR
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.anyOf(".-")).precomputed();

    private static final CharMatcher SQUOTE = CharMatcher.is('\'');
    private static final CharMatcher DQUOTE = CharMatcher.is('"');

    private static final char SLASH = '/';
    private static final char COLON = ':';
    private static final char DOT = '.';
    private static final char EQUALS = '=';
    private static final char PRECONDITION_START = '[';
    private static final char PRECONDITION_END = ']';

    private final AbstractStringInstanceIdentifierCodec codec;
    private final String data;

    private final LinkedList<PathArgument> product = new LinkedList<>();

    private DataSchemaContextNode<?> current;
    private int offset;

    XpathStringParsingPathArgumentBuilder(AbstractStringInstanceIdentifierCodec codec, String data) {
        this.codec = Preconditions.checkNotNull(codec);
        this.data = Preconditions.checkNotNull(data);
        this.current = codec.getDataContextTree().getRoot();
        this.offset = 0;
    }


    @Override
    public Iterable<PathArgument> build() {
        while (!allCharactersConsumed()) {
            product.add(computeNextArgument());
        }
        return ImmutableList.copyOf(product);
    }

    private PathArgument computeNextArgument() {
        checkValid(SLASH  == currentChar(),"Identifier must start with '/'.");
        skipCurrentChar();

        QName name = nextQName();
        if(allCharactersConsumed() || SLASH == currentChar()) {
            return computeIdentifier(name);
        } else {
            checkValid(PRECONDITION_START == currentChar(), "Last element must be identifier, predicate or '/'");
            return computeIdentifierWithPredicate(name);
        }
    }


    private DataSchemaContextNode<?> nextContextNode(QName name) {
        current = current.getChild(name);
        checkValid(current != null, "%s is not correct schema node identifier.",name);
        while(current.isMixin()) {
            product.add(current.getIdentifier());
            current = current.getChild(name);
        }
        return current;
    }


    /**
     *
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
    private PathArgument computeIdentifierWithPredicate(QName name) {
        DataSchemaContextNode<?> currentNode = nextContextNode(name);
        checkValid(currentNode.isKeyedEntry(), "Entry %s does not allow specifying predicates.", name);

        ImmutableMap.Builder<QName,Object> keyValues = ImmutableMap.builder();
        while(!allCharactersConsumed() && PRECONDITION_START == currentChar()) {
            skipCurrentChar();
            skipWhitespaces();
            final QName key;
            if(DOT == currentChar()) {
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
            if(key == null && currentNode.isLeaf()) {
                checkValid(offset == data.length(), "Leaf argument must be last argument of instance identifier.");
                return new YangInstanceIdentifier.NodeWithValue(name, keyValue);
            }
            final DataSchemaContextNode<?> keyNode = currentNode.getChild(key);
            checkValid(keyNode != null, "%s is not correct schema node identifier.", key);
            final Object value = codec.deserializeKeyValue(keyNode.getDataSchemaNode(), keyValue);
            keyValues.put(key, value);
        }
        return new YangInstanceIdentifier.NodeIdentifierWithPredicates(name, keyValues.build());
    }


    private PathArgument computeIdentifier(QName name) {
        DataSchemaContextNode<?> currentNode = nextContextNode(name);
        checkValid(!currentNode.isKeyedEntry(), "Entry %s requires key or value predicate to be present", name);
        return currentNode.getIdentifier();
    }


    /**
     *
     * Returns following QName and sets offset to end of QName.
     *
     * @return following QName.
     */
    private QName nextQName() {
        // Consume prefix or identifie
        final String maybePrefix = nextIdentifier();
        final String prefix,localName;
        if(COLON == currentChar()) {
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
     * Returns true if all characters from input string
     * were consumed.
     *
     * @return true if all characters from input string
     * were consumed.
     */
    private boolean allCharactersConsumed() {
        return offset == data.length();
    }


    private QName createQName(String prefix, String localName) {
        return codec.createQName(prefix, localName);
    }

    /**
     *
     * Skips current char if it equals expected otherwise fails parsing.
     *
     * @param expected Expected character
     * @param errorMsg Error message if {@link #currentChar()} does not match expected.
     */
    private void checkCurrentAndSkip(char expected, String errorMsg) {
        checkValid(expected == currentChar(), errorMsg);
        offset++;
    }


    /**
     *
     * Deserializes value for supplied key
     *
     * @param key Name of referenced key, If null, referenced leaf is previous encountered item.
     * @param value Value to be checked and deserialized
     * @return Object representing value in yang-data-api format.
     */
    private Object deserializeValue(@Nullable QName key, String value) {
        // FIXME: Use codec to deserialize value to correct Java type
        return value;
    }

    /**
     *
     * Fails parsing if condition is not met.
     *
     * In case of error provides pointer to failed instance identifier,
     * offset on which failure occured with explanation.
     *
     * @param condition Fails parsing if {@code condition} is false
     * @param errorMsg Error message which will be provided to user.
     * @param attributes
     */
    private void checkValid(boolean condition, String errorMsg, Object... attributes) {
        Preconditions.checkArgument(condition, "Could not parse Instance Identifier '%s'. Offset: %s : Reason: %s",
                data,
                offset,
                String.format(errorMsg, attributes));
    }

    /**
     *
     * Returns following value of quoted literal (without qoutes)
     * and sets offset after literal.
     *
     * @return String literal
     */
    private String nextQuotedValue() {
        char quoteChar = currentChar();
        checkValidQuotation(quoteChar);
        skipCurrentChar();
        int valueStart = offset;
        int endQoute = data.indexOf(quoteChar, offset);
        String value = data.substring(valueStart, endQoute);
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
     * Increases processing offset by 1
     */
    private void skipCurrentChar() {
        offset++;
    }

    /**
     * Skip whitespace characters, sets offset to first following
     * non-whitespace character.
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
        int start = offset;
        checkValid(IDENTIFIER_FIRST_CHAR.matches(currentChar()), "Identifier must start with character from set 'a-zA-Z_'");
        nextSequenceEnd(IDENTIFIER);
        return data.substring(start, offset);
    }

    private void nextSequenceEnd(CharMatcher matcher) {
        while(!allCharactersConsumed() && matcher.matches(data.charAt(offset))) {
            offset++;
        }
    }

    private void checkValidQuotation(char quoteChar) {
        checkValid(
                SQUOTE.matches(quoteChar) || DQUOTE.matches(quoteChar),
                "Value must be qoute escaped with ''' or '\"'.");

    }

}
