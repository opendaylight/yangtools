/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public sealed interface CharacterProperty extends PatternFragment {
    record IsBlock(String name) implements CharacterProperty {
        public IsBlock {
            requireNonNull(name);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            // Multiple ways to do this:
            // - InFoo
            // - block=Foo
            // - blk=Foo
            // We choose the most similar
            sb.append("In").append(name);
        }
    }

    enum IsCategory implements CharacterProperty {
        LETTERS("L"),
        LETTERS_U("Lu"),
        LETTERS_L("Ll"),
        LETTERS_T("Lt"),
        LETTERS_M("Lm"),
        LETTERS_O("Lo"),
        MARKS("M"),
        MARKS_N("Mn"),
        MARKS_C("Mc"),
        MARKS_E("Me"),
        NUMBERS("N"),
        NUMBERS_D("Nd"),
        NUMBERS_L("Nl"),
        NUMBERS_O("No"),
        PUNCTUATION("P"),
        PUNCTUATION_C("Pc"),
        PUNCTUATION_D("Pd"),
        PUNCTUATION_S("Ps"),
        PUNCTUATION_E("Pe"),
        PUNCTUATION_I("Pi"),
        PUNCTUATION_F("Pf"),
        PUNCTUATION_O("Po"),
        SEPARATORS("Z"),
        SEPARATORS_S("Zs"),
        SEPARATORS_L("Zl"),
        SEPARATORS_P("Zp"),
        SYMBOLS("S"),
        SYMBOLS_M("Sm"),
        SYMBOLS_C("Sc"),
        SYMBOLS_K("Sk"),
        SYMBOLS_O("So"),
        OTHERS("C"),
        OTHERS_C("Cc"),
        OTHERS_F("Cf"),
        OTHERS_O("Co"),
        OTHERS_N("Cn");

        private final String str;

        IsCategory(final String str) {
            this.str = requireNonNull(str);
        }

        public static @Nullable IsCategory forLiteral(final String str) {
            return switch (str) {
                case "L" -> LETTERS;
                case "Lu" -> LETTERS_U;
                case "Ll" -> LETTERS_L;
                case "Lt" -> LETTERS_T;
                case "Lm" -> LETTERS_M;
                case "Lo" -> LETTERS_O;
                case "M" -> MARKS;
                case "Mn" -> MARKS_N;
                case "Mc" -> MARKS_C;
                case "Me" -> MARKS_E;
                case "N" -> NUMBERS;
                case "Nd" -> NUMBERS_D;
                case "Nl" -> NUMBERS_L;
                case "No" -> NUMBERS_O;
                case "P" -> PUNCTUATION;
                case "Pc" -> PUNCTUATION_C;
                case "Pd" -> PUNCTUATION_D;
                case "Ps" -> PUNCTUATION_S;
                case "Pe" -> PUNCTUATION_E;
                case "Pi" -> PUNCTUATION_I;
                case "Pf" -> PUNCTUATION_F;
                case "Po" -> PUNCTUATION_O;
                case "Z" -> SEPARATORS;
                case "Zs" -> SEPARATORS_S;
                case "Zl" -> SEPARATORS_L;
                case "Zp" -> SEPARATORS_P;
                case "S" -> SYMBOLS;
                case "Sm" -> SYMBOLS_M;
                case "Sc" -> SYMBOLS_C;
                case "Sk" -> SYMBOLS_K;
                case "So" -> SYMBOLS_O;
                case "C" -> OTHERS;
                case "Cc" -> OTHERS_C;
                case "Cf" -> OTHERS_F;
                case "Co" -> OTHERS_O;
                case "Cn" -> OTHERS_N;
                default -> null;
            };
        }

        public static IsCategory ofLiteral(final String str) {
            final var ret = forLiteral(str);
            if (ret == null) {
                throw new IllegalArgumentException("Invalid category " + str);
            }
            return ret;
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }
}
