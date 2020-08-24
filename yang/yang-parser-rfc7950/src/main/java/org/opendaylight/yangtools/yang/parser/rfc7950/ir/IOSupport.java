/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import com.google.common.annotations.Beta;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class IOSupport {
    static final byte STMT_022       = 0x01;
    static final byte STMT_031       = 0x02;
    static final byte STMT_044       = 0x03;
    static final byte STMT_144       = 0x04;
    static final byte STMT_N44_U8    = 0x05;
    static final byte STMT_N44_U16   = 0x06;
    static final byte STMT_N44_S32   = 0x07;
    static final byte MASK_STMT      = STMT_N44_S32;

    static final byte ARG_NONE       = 0x00;
    static final byte ARG_IDENTIFIER = 0x08;
    static final byte ARG_DQUOT      = 0x10;
    static final byte ARG_SQUOT      = 0x18;
    static final byte ARG_UQUOT      = 0x20;
    static final byte ARG_CONCAT_U8  = 0x28;
    static final byte ARG_CONCAT     = 0x38;
    static final byte MASK_ARG       = ARG_CONCAT;

    static final byte KEY_QUAL       = 0x00;
    static final byte KEY_UQUAL      = 0x40;
    static final byte MASK_KEY       = KEY_UQUAL;

    static final byte STR_REF_31     = 0x00;
    static final byte STR_REF_U8     = 0x20;
    static final byte STR_REF_U16    = 0x40;
    static final byte STR_REF_S32    = 0x60;
    static final byte STR_DEF_UTF    = (byte) 0x80; // writeUTF(), <16384
    static final byte STR_DEF_U16    = (byte) 0xA0; // short + UTF
    static final byte STR_DEF_S32    = (byte) 0xC0; // int + URF
    static final byte STR_DEF_CHARS  = (byte) 0xE0; // writeChars()
    static final byte MASK_STR_TYPE  = STR_DEF_CHARS;
    static final byte MASK_STR_LEN   = 0x1F;
    static final byte SHIFT_STR_LEN  = 5;

    private IOSupport() {
        // Hidden on purpose
    }

    public static void writeStatement(final DataOutput out, final IRStatement statement) throws IOException {
        new StatementOutput(out).writeStatement(statement);
    }

    public static @NonNull IRStatement readStatement(final DataInput in) throws IOException {
        return new StatementInput(in).readStatement();
    }
}
