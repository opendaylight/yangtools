/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Concatenation;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.DoubleQuoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Identifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.SingleQuoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Unquoted;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Unqualified;

final class StatementInput {
    private final List<String> codedStrings = new ArrayList<>();
    private final DataInput in;

    StatementInput(final DataInput in) {
        this.in = requireNonNull(in);
    }

    @NonNull IRStatement readStatement() throws IOException {
        final byte header = in.readByte();

        final int key = header & IOSupport.MASK_KEY;
        final IRKeyword keyword;
        switch (key) {
            case IOSupport.KEY_QUAL:
                keyword = new Qualified(readString(), readString());
                break;
            case IOSupport.KEY_UQUAL:
                keyword = new Unqualified(readString());
                break;
            default:
                throw new IOException("Unhandled key " + key);
        }

        final int arg = header & IOSupport.MASK_ARG;
        final IRArgument argument;
        switch (arg) {
            case IOSupport.ARG_NONE:
                argument = null;
                break;
            // FIXME: share definitions
            case IOSupport.ARG_IDENTIFIER:
                argument = new Identifier(readString());
                break;
            case IOSupport.ARG_DQUOT:
                argument = new DoubleQuoted(readString());
                break;
            case IOSupport.ARG_SQUOT:
                argument = new SingleQuoted(readString());
                break;
            case IOSupport.ARG_UQUOT:
                argument = new Unquoted(readString());
                break;
            case IOSupport.ARG_CONCAT:
                argument = readConcat(in.readInt());
                break;
            case IOSupport.ARG_CONCAT_U8:
                argument = readConcat(in.readByte());
                break;
            default:
                throw new IOException("Unhandled argument " + arg);
        }

        final int stmt = header & IOSupport.MASK_STMT;
        switch (stmt) {
            case IOSupport.STMT_022:
                return new IRStatement022(keyword, argument, in.readShort(), in.readShort());
            case IOSupport.STMT_031:
                final int value = in.readInt();
                return new IRStatement031(keyword, argument, value >>> 8, value & 0xFF);
            case IOSupport.STMT_044:
                return new IRStatement044(keyword, argument, in.readInt(), in.readInt());
            case IOSupport.STMT_144:
                return new IRStatement144(keyword, argument, in.readInt(), in.readInt(), readStatement());
            case IOSupport.STMT_N44_U8:
                return new IRStatementL44(keyword, argument, in.readInt(), in.readInt(), readStatements(in.readByte()));
            case IOSupport.STMT_N44_U16:
                return new IRStatementL44(keyword, argument, in.readInt(), in.readInt(),
                    readStatements(in.readShort()));
            case IOSupport.STMT_N44_S32:
                return new IRStatementL44(keyword, argument, in.readInt(), in.readInt(), readStatements(in.readInt()));
            default:
                throw new IOException("Unhandled statement " + stmt);
        }
    }

    private Concatenation readConcat(final int count) throws IOException {
        final ImmutableList.Builder<Single> builder = ImmutableList.builderWithExpectedSize(count);
        for (int i = 0; i < count; ++i) {
            builder.add(readSingleArgument());
        }
        return new Concatenation(builder.build());
    }

    private Single readSingleArgument() throws IOException {
        final byte header = in.readByte();
        switch (header) {
            // FIXME: share definitions
            case IOSupport.ARG_IDENTIFIER:
                return new Identifier(readString());
            case IOSupport.ARG_DQUOT:
                return new DoubleQuoted(readString());
            case IOSupport.ARG_SQUOT:
                return new SingleQuoted(readString());
            case IOSupport.ARG_UQUOT:
                return new Unquoted(readString());
            default:
                throw new IOException("Unhandled single argument " + header);
        }
    }

    private ImmutableList<IRStatement> readStatements(final int count) throws IOException {
        final ImmutableList.Builder<IRStatement> builder = ImmutableList.builderWithExpectedSize(count);
        for (int i = 0; i < count; ++i) {
            builder.add(readStatement());
        }
        return builder.build();
    }

    private String readString() throws IOException {
        final byte header = in.readByte();
        final int type = header & IOSupport.MASK_STR_TYPE;
        switch (type) {
            case IOSupport.STR_DEF_UTF:
                return defineString(in.readUTF());
            case IOSupport.STR_DEF_U16:
                return defineString(readStringBytes(in.readUnsignedShort()));
            case IOSupport.STR_DEF_S32:
                return defineString(readStringBytes(in.readInt()));
            case IOSupport.STR_DEF_CHARS:
                return defineString(readStringChars());
            case IOSupport.STR_REF_31:
                return lookupString(header & IOSupport.MASK_STR_LEN);
            case IOSupport.STR_REF_U8:
                return lookupString((in.readUnsignedByte() << IOSupport.SHIFT_STR_LEN) + 32);
            case IOSupport.STR_REF_U16:
                return lookupString((in.readUnsignedShort() << IOSupport.SHIFT_STR_LEN) + 8224);
            case IOSupport.STR_REF_S32:
                return lookupString(in.readInt());
            default:
                throw new IOException("Unhandled string type " + type + " in header " + header);
        }
    }

    private String readStringBytes(final int size) throws IOException {
        if (size > 0) {
            final byte[] bytes = new byte[size];
            in.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } else if (size == 0) {
            return "";
        } else {
            throw new IOException("Invalid String bytes length " + size);
        }
    }

    private String readStringChars() throws IOException {
        final int size = in.readInt();
        if (size > 0) {
            final char[] chars = new char[size];
            for (int i = 0; i < size; ++i) {
                chars[i] = in.readChar();
            }
            return String.valueOf(chars);
        } else if (size == 0) {
            return "";
        } else {
            throw new IOException("Invalid String chars length " + size);
        }
    }

    private String defineString(final String str) {
        codedStrings.add(str);
        return str;
    }

    private String lookupString(final int offset) throws IOException {
        try {
            return codedStrings.get(offset);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Invalid String reference " + offset, e);
        }
    }
}
