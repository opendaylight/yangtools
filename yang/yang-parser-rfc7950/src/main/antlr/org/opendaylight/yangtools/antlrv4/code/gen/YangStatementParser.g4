//
// Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//
parser grammar YangStatementParser;

options {
    tokenVocab = YangStatementLexer;
}

// NOTE: we need to use SEP*/SEP+ because comments end up breaking whitespace
//       sequences into two.
file : SEP* statement SEP* EOF;
statement : keyword (SEP+ argument)? SEP* (SEMICOLON | LEFT_BRACE SEP* (statement SEP*)* RIGHT_BRACE);
keyword : IDENTIFIER (COLON IDENTIFIER)?;

// Alright, so what constitutes a string is rather funky. We need to deal with
// the flaky definitions of RFC6020, which allow for insane quoting as well as
// exclusion of comments. We also need to allow for stitching back tokens like
// PLUS/COLON, which may end up being valid identifiers.
argument :
    // Note on optimization: we are allowing a single IDENTIFIER, although it
    // is already part of unquotedString. This is strictly superfluous, but a
    // single IDENTIFIER arguments are very common and this eliminates an
    // indirection costing us at least two objects. This is not quite a case
    // of premature optimization, but rather IDENTIFIER is really so very
    // special and deserving of this treatment.
    IDENTIFIER
    |
    // Quoted string and concatenations thereof. We are sacrificing brewity
    // here to eliminate the need for another parser construct. Quoted strings
    // account for about 50% of all arguments encountered -- hence the added
    // parse tree indirection is very visible.
    (DQUOT_START DQUOT_STRING? DQUOT_END | SQUOT_START SQUOT_STRING? SQUOT_END)
    (SEP* PLUS SEP* (DQUOT_START DQUOT_STRING? DQUOT_END | SQUOT_START SQUOT_STRING? SQUOT_END))*
    |
    unquotedString
    ;

unquotedString :
    SLASH | STAR+
    |

    // Alright this is written in a non-trivial manner due to us wanting to
    // keep the number of parser objects (and hence memory pressure) down.
    //
    // Our aim is to forbid '//', '/*' and '*/' from being accepted as a
    // valid unquoted string. Normally we would write this as a recursive
    // parser rule for concatenating on '*' and '/' and let ANTLR figure it
    // out. Unfortunately that results in a deep parse tree, essentially
    // having one level for each such concatenation. For a test case imagine
    // how "a*b/c*d*e**f" would get parsed with a recursive grammar.
    //
    // Now we cannot do much aboud tokenization, but we can statically express
    // the shape we are looking for:

    //   so an unquoted string may optionally start with a single SLASH or any
    //   number of STARs ...
    (SLASH? | STAR*)

    //   ... but that needs to be followed by at least one span of other
    //       content, which is what we are really aiming for. This ensures
    //       any leading SLASH/STAR is followed by a non-(SLASH|STAR) ...
    (COLON | PLUS | IDENTIFIER | UQUOT_STRING)+

    //   ... and based on that knowledge, we allow another SLASH or run of
    //       STARs to follow, but it has to be again followed by a run of
    //       of other tokens -- and rinse&repeat that any number of times.
    //       We still have ensured that the span matched does not end with
    //       a SLASH or a STAR ...
    //       ways retaining the 'does not end with SLASH or STAR' invariant
    ((SLASH | STAR+) (COLON | PLUS | IDENTIFIER | UQUOT_STRING)+)*

    //   ... and therefore it is always safe to have such a span end with
    //       a SLASH or STARs.
    (SLASH? | STAR*)
    ;
