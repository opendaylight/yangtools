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
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.PathMixin;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.SimpleValue;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Iterator which lazily parses {@link PathArgument} from string representation.
 *
 * <p>
 * Note that invocation of {@link #hasNext()} or {@link #next()} may result in
 * throwing of {@link IllegalArgumentException} if underlying string representation
 * is not correctly serialized or does not represent instance identifier valid
 * for associated schema context.
 */
final class XpathStringParsingPathArgumentBuilder implements Mutable {
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

    private static final char SLASH = '/';
    private static final char BACKSLASH = '\\';
    private static final char COLON = ':';
    private static final char DOT = '.';
    private static final char EQUALS = '=';
    private static final char PRECONDITION_START = '[';
    private static final char PRECONDITION_END = ']';
    private static final char SQUOT = '\'';
    private static final char DQUOT = '"';

    private final List<PathArgument> product = new ArrayList<>();
    private final AbstractStringInstanceIdentifierCodec codec;
    private final SchemaInferenceStack stack;
    private final String data;

    private DataSchemaContext current;
    private QNameModule lastModule;
    private int offset;

    XpathStringParsingPathArgumentBuilder(final AbstractStringInstanceIdentifierCodec codec, final String data) {
        this.codec = requireNonNull(codec);
        this.data = requireNonNull(data);
        offset = 0;

        final DataSchemaContextTree tree = codec.getDataContextTree();
        stack = SchemaInferenceStack.of(tree.modelContext());
        current = tree.getRoot();
    }

    /**
     * Parse input string and return the corresponding list of {@link PathArgument}s.
     *
     * @return List of PathArguments
     * @throws IllegalArgumentException if the input string is not valid
     */
    @NonNull List<PathArgument> build() {
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
        // Memoize module
        lastModule = name.getModule();
        if (allCharactersConsumed() || SLASH == currentChar()) {
            return computeIdentifier(name);
        }

        checkValid(PRECONDITION_START == currentChar(), "Last element must be identifier, predicate or '/'");
        return computeIdentifierWithPredicate(name);
    }

    private DataSchemaContext nextContextNode(final QName qname) {
        current = getChild(current, qname);
        checkValid(current != null, "%s is not correct schema node identifier.", qname);
        while (current instanceof PathMixin mixin) {
            product.add(mixin.mixinPathStep());
            current = getChild(current, qname);
        }
        stack.enterDataTree(qname);
        return current;
    }

    private static DataSchemaContext getChild(final DataSchemaContext parent, final QName qname) {
        return parent instanceof Composite composite ? composite.childByQName(qname) : null;
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
        final var currentNode = nextContextNode(name);
        if (currentNode.pathStep() != null) {
            throw iae("Entry %s does not allow specifying predicates.", name);
        }

        final var keyValues = ImmutableMap.<QName, Object>builder();
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
            if (key == null && currentNode instanceof SimpleValue) {
                checkValid(offset == data.length(), "Leaf argument must be last argument of instance identifier.");
                final var currentSchema = currentNode.dataSchemaNode();

                final Object value = codec.deserializeKeyValue(currentSchema,
                    type -> resolveLeafref(currentSchema.getQName(), type), keyValue);
                return new NodeWithValue<>(name, value);
            }
            final var keyNode = currentNode instanceof Composite composite ? composite.childByQName(key) : null;
            if (keyNode == null) {
                throw iae("%s is not correct schema node identifier.", key);
            }

            final Object value = codec.deserializeKeyValue(keyNode.dataSchemaNode(),
                type -> resolveLeafref(key, type), keyValue);
            keyValues.put(key, value);
        }
        return NodeIdentifierWithPredicates.of(name, keyValues.build());
    }

    private @NonNull TypeDefinition<?> resolveLeafref(final QName qname, final LeafrefTypeDefinition type) {
        final SchemaInferenceStack tmp = stack.copy();
        tmp.enterDataTree(qname);
        return tmp.resolveLeafref(type);
    }

    private @NonNull NodeIdentifier computeIdentifier(final QName name) {
        final var currentNode = nextContextNode(name);
        final var currentArg = currentNode.pathStep();
        if (currentArg == null) {
            throw iae("Entry %s requires key or value predicate to be present", name);
        }
        return currentArg;
    }

    /**
     * Returns following QName and sets offset to end of QName.
     *
     * @return following QName.
     */
    private @NonNull QName nextQName() {
        // Consume prefix or identifier
        final String maybePrefix = nextIdentifier();
        if (!allCharactersConsumed() && COLON == currentChar()) {
            // previous token is prefix
            skipCurrentChar();
            return codec.createQName(maybePrefix, nextIdentifier());
        }

        return codec.createQName(lastModule, maybePrefix);
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
            throw iae(errorMsg, attributes);
        }
    }

    private @NonNull IllegalArgumentException iae(final String errorMsg, final Object... attributes) {
        return new IllegalArgumentException("Could not parse Instance Identifier '%s'. Offset: %s : Reason: %s"
            .formatted(data, offset, errorMsg.formatted(attributes)));
    }

    /**
     * Returns following value of quoted literal (without quotes) and sets offset after literal.
     *
     * @return String literal
     */
    private String nextQuotedValue() {
        return switch (currentChar()) {
            case SQUOT -> nextSingleQuotedValue();
            case DQUOT -> nextDoubleQuotedValue();
            default -> throw iae("Value must be quote escaped with ''' or '\"'.");
        };
    }

    // Simple: just look for the matching single quote and return substring
    private String nextSingleQuotedValue() {
        skipCurrentChar();
        final int start = offset;
        final int end = data.indexOf(SQUOT, start);
        checkValid(end != -1, "Closing single quote not found");
        offset = end;
        skipCurrentChar();
        return data.substring(start, end);
    }

    // Complicated: we need to potentially un-escape
    private String nextDoubleQuotedValue() {
        skipCurrentChar();

        final int maxIndex = data.length() - 1;
        final var sb = new StringBuilder();
        while (true) {
            final int nextStart = offset;

            // Find next double quotes
            final int nextEnd = data.indexOf(DQUOT, nextStart);
            checkValid(nextEnd != -1, "Closing double quote not found");
            offset = nextEnd;

            // Find next backslash
            final int nextBackslash = data.indexOf(BACKSLASH, nextStart);
            if (nextBackslash == -1 || nextBackslash > nextEnd) {
                // No backslash between nextStart and nextEnd -- just copy characters and terminate
                offset = nextEnd;
                skipCurrentChar();
                return sb.append(data, nextStart, nextEnd).toString();
            }

            // Validate escape completeness and append buffer
            checkValid(nextBackslash != maxIndex, "Incomplete escape");
            sb.append(data, nextStart, nextBackslash);

            // Adjust offset before potentially referencing it and
            offset = nextBackslash;
            sb.append(unescape(data.charAt(nextBackslash + 1)));

            // Rinse and repeat
            offset = nextBackslash + 2;
        }
    }

    // As per https://www.rfc-editor.org/rfc/rfc7950#section-6.1.3
    private char unescape(final char escape) {
        return switch (escape) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case DQUOT -> DQUOT;
            case BACKSLASH -> BACKSLASH;
            default -> throw iae("Unrecognized escape");
        };
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
