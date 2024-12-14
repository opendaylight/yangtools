/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static com.google.common.base.Verify.verifyNotNull;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.DASH;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.SingleCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.XmlChar;
import static org.opendaylight.yangtools.yang.parser.rfc7950.xsd.AbstractRegularExpressionVisitor.childToken;
import static org.opendaylight.yangtools.yang.parser.rfc7950.xsd.AbstractRegularExpressionVisitor.parseCharClassEscape;
import static org.opendaylight.yangtools.yang.parser.rfc7950.xsd.AbstractRegularExpressionVisitor.verifyChildTerminal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassEscContext;

public final class RegularExpressionParser extends AbstractRegularExpressionVisitor {
    private final ArrayDeque<Object> stack = new ArrayDeque<>();

    private RegularExpression expr;

    private RegularExpressionParser() {
        // Hidden on purpose
    }

    @NonNullByDefault
    public static RegularExpression parse(final StatementSourceReference ref, final String str) {
        final var parser = new RegularExpressionParser();
        parser.visitRoot(AntlrSupport.parseRegularExpression(ref, str));
        return verifyNotNull(parser.expr);
    }

    @Override
    void startExpression(final int expectedBranchCount) {
        final var branches = new ArrayList<Branch>();

    }

    @Override
    void endExpression() {
        // expr = new RegularExpression(branches);

    }

    @Override
    void emptyBranch() {
        // branches.add(Branch.EMPTY)
    }

    @Override
    void startBranch(final int size) {
        // FIXME: push branch builder
        final var pieces = new ArrayList<Piece>(size);

    }

    @Override
    void endBranch() {
        // branches.pop() + build + branches.add()
        //      return new Branch(pieces);
    }

    @Override
    void startPiece(final SingleCharacterEscape escape) {

    }

    @Override
    void startPiece(final MultiCharacterEscape escape) {

    }

    @Override
    void startPiece(final Dot wildcard) {

    }

    @Override
    void startPiece(final CategoryEscape escape) {

    }

    @Override
    void startPiece(final ComplementEscape escape) {

    }

    @Override
    void endPiece() {
//        new Piece(atom, null);
    }

    @Override
    void endPiece(final Quantifier quantifier) {
//      new Piece(atom, quantifier);
    }

    // TODO: Re-formulate this logic in terms of events ands Builders. A superclass should contain the traversal logic,
    //       and it should issue callouts to emit individual parts. This class should maintain a stack of builders, with
    //       the topmost being RegularExpression.Builder and its callouts should manipulate the stack/builders. Once we
    //       have traversed RegExpComplete, we should end up with a stack containing the initial builder, and we should
    //       return its product.

    private static void fillPosCharGroup(final ArrayList<CharacterGroup> components, final List<ParseTree> children,
            final int start, final int size) {
        int offset = start;
        while (offset < size) {
            final var first = children.get(offset++);
            if (first instanceof CharClassEscContext charClassEsc) {
                components.add(parseCharClassEscape(charClassEsc));
                continue;
            }

            final var token = verifyChildTerminal(first);
            switch (token.getType()) {
                case SingleCharEsc -> verifyToken(childToken(children, offset), DASH);
                case XmlChar -> {
                    // TODO: the way this is structured means we throw away the next child and re-lookup/check it again
                    if (offset == size || !(children.get(offset) instanceof TerminalNode next)
                        || next.getSymbol().getType() != DASH) {
                        // XmlChar followed by nothing or non-DASH: append proceed to next
                        components.add(new SimpleCharacterGroup(token.getText()));
                        continue;
                    }
                }
                default -> throw unexpectedToken(token);
            }

            // token is start of range, offset points to a DASH, now let's see the third token
            components.add(new RangeCharacterGroup(parseCharOrEsc(token),
                parseCharOrEsc(childToken(children, offset + 1))));
            offset += 2;
        }
    }

    private static String parseCharOrEsc(final Token token) {
        return switch (token.getType()) {
            case SingleCharEsc, XmlChar -> token.getText();
            default -> throw unexpectedToken(token);
        };
    }

    private static CharacterGroup union(final CharacterGroup first, final CharacterGroup second) {
        if (first instanceof UnionCharacterGroup(var components)) {
            return appendGroup(components, second);
        }
        if (second instanceof UnionCharacterGroup(var components)) {
            return prependGroup(components, first);
        }
        if (first instanceof SimpleCharacterGroup(var fstr) && second instanceof SimpleCharacterGroup(var sstr)) {
            return new SimpleCharacterGroup(fstr + sstr);
        }
        return new UnionCharacterGroup(List.of(first, second));
    }

    private static CharacterGroup appendGroup(final List<CharacterGroup> components, final CharacterGroup group) {
        final var tmp = new ArrayList<CharacterGroup>(components.size() + 1);
        tmp.addAll(components);
        // TODO: SimpleCharacterGroup merge if applicable
        tmp.add(group);
        return new UnionCharacterGroup(tmp);
    }

    private static CharacterGroup prependGroup(final List<CharacterGroup> components, final CharacterGroup group) {
        final var tmp = new ArrayList<CharacterGroup>(components.size() + 1);
        // TODO: SimpleCharacterGroup merge if applicable
        tmp.add(group);
        tmp.addAll(components);
        return new UnionCharacterGroup(tmp);
    }
}
