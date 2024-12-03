/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

// FIXME:
@NonNullByDefault
public sealed interface IsCategory extends CharacterProperty {

    enum Letters implements IsCategory {
        DEFAULT("L"),
        U("Lu"),
        L("Ll"),
        T("Lt"),
        M("Lm"),
        O("Lo");

        private final String str;

        Letters(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Marks implements IsCategory {
        DEFAULT("M"),
        N("Mn"),
        C("Mc"),
        E("Me");

        private final String str;

        Marks(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Numbers implements IsCategory {
        DEFAULT("N"),
        D("Nd"),
        L("Nl"),
        O("No");

        private final String str;

        Numbers(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Punctuation implements IsCategory {
        DEFAULT("P"),
        C("Pc"),
        D("Pd"),
        S("Ps"),
        E("Pe"),
        I("Pi"),
        F("Pf"),
        O("Po");

        private final String str;

        Punctuation(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Separators implements IsCategory {
        DEFAULT("Z"),
        S("Zs"),
        L("Zl"),
        P("Zp");

        private final String str;

        Separators(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Symbols implements IsCategory {
        DEFAULT("S"),
        M("Sm"),
        C("Sc"),
        K("Sk"),
        O("So");

        private final String str;

        Symbols(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    enum Others implements IsCategory {
        DEFAULT("C"),
        C("Cc"),
        F("Cf"),
        O("Co"),
        N("Cn");

        private final String str;

        Others(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void appendPatternFragment(final StringBuilder sb) {
            sb.append(str);
        }
    }

    static IsCategory ofLiteral(final String str) {
        return switch (str) {
            case "L" -> Letters.DEFAULT;
            case "Lu" -> Letters.U;
            case "Ll" -> Letters.L;
            case "Lt" -> Letters.T;
            case "Lm" -> Letters.M;
            case "Lo" -> Letters.O;
            case "M" -> Marks.DEFAULT;
            case "Mn" -> Marks.N;
            case "Mc" -> Marks.C;
            case "Me" -> Marks.E;
            case "N" -> Numbers.DEFAULT;
            case "Nd" -> Numbers.D;
            case "Nl" -> Numbers.L;
            case "No" -> Numbers.O;
            case "P" -> Punctuation.DEFAULT;
            case "Pc" -> Punctuation.D;
            case "Pd" -> Punctuation.D;
            case "Ps" -> Punctuation.S;
            case "Pe" -> Punctuation.E;
            case "Pi" -> Punctuation.I;
            case "Pf" -> Punctuation.F;
            case "Po" -> Punctuation.O;
            case "Z" -> Separators.DEFAULT;
            case "Zs" -> Separators.S;
            case "Zl" -> Separators.L;
            case "Zp" -> Separators.P;
            case "S" -> Symbols.DEFAULT;
            case "Sm" -> Symbols.M;
            case "Sc" -> Symbols.C;
            case "Sk" -> Symbols.K;
            case "So" -> Symbols.O;
            case "C" -> Others.DEFAULT;
            case "Cc" -> Others.C;
            case "Cf" -> Others.F;
            case "Co" -> Others.O;
            case "Cn" -> Others.N;
            default -> throw new IllegalArgumentException("Unhandled value " + str);
        };
    }
}
