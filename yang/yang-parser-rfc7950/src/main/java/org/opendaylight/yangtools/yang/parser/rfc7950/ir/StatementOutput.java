/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Single;

final class StatementOutput {
    private final Map<String, Integer> strings = new HashMap<>();
    private final DataOutput out;

    StatementOutput(final DataOutput out) {
        this.out = requireNonNull(out);
    }

    void writeStatement(final IRStatement statement) throws IOException {
        final byte stmt = statement.ioType();
        final IRArgument argument = statement.argument();
        final byte arg = argument == null ? IOSupport.ARG_NONE : argument.ioType();
        final IRKeyword keyword = statement.keyword();
        final byte key = keyword.ioType();

        out.writeByte(stmt | arg | key);
        switch (key) {
            case IOSupport.KEY_QUAL:
                writeString(verifyNotNull(keyword.prefix()));
                writeString(keyword.identifier());
                break;
            case IOSupport.KEY_UQUAL:
                writeString(keyword.identifier());
                break;
            default:
                throw new IOException("Internal error: unhandled key " + key);
        }

        switch (arg) {
            case IOSupport.ARG_NONE:
                // No-op
                break;
            case IOSupport.ARG_IDENTIFIER:
            case IOSupport.ARG_DQUOT:
            case IOSupport.ARG_SQUOT:
            case IOSupport.ARG_UQUOT:
                writeString(((Single) argument).string());
                break;
            case IOSupport.ARG_CONCAT:
            case IOSupport.ARG_CONCAT_U8:
                // FIXME: implement this
            default:
                throw new IOException("Internal error: unhandled argument " + arg);
        }

        final List<? extends IRStatement> statements = statement.statements();
        switch (stmt) {
            case IOSupport.STMT_022:
                out.writeShort(statement.startLine());
                out.writeShort(statement.startColumn());
                break;
            case IOSupport.STMT_031:
                out.writeInt(((IRStatement031) statement).value());
                break;
            case IOSupport.STMT_044:
                writeLocation(statement);
                break;
            case IOSupport.STMT_144:
                writeLocation(statement);
                writeStatement(statements.get(0));
                break;
            case IOSupport.STMT_N44_U8:
                writeLocation(statement);
                out.writeByte(statements.size());
                writeStatements(statements);
                break;
            case IOSupport.STMT_N44_U16:
                writeLocation(statement);
                out.writeShort(statements.size());
                writeStatements(statements);
                break;
            case IOSupport.STMT_N44_S32:
                writeLocation(statement);
                out.writeInt(statements.size());
                writeStatements(statements);
                break;
            default:
                throw new IOException("Internal error: unhandled statement " + stmt);
        }
    }

    private void writeLocation(final IRStatement statement) throws IOException {
        out.writeInt(statement.startLine());
        out.writeInt(statement.startColumn());
    }

    private void writeStatements(final List<? extends IRStatement> statements) throws IOException {
        for (IRStatement statement : statements) {
            writeStatement(statement);
        }
    }

    private void writeString(final @NonNull String str) throws IOException {
        final Integer key = strings.get(str);
        if (key != null) {
            writeStringRef(key.intValue());
        } else {
            writeStringDef(str);
        }
    }

    private void writeStringDef(final @NonNull String str) throws IOException {
        strings.put(str, strings.size());

        if (str.length() <= Short.MAX_VALUE / 2) {
            out.writeByte(IOSupport.STR_DEF_UTF);
            out.writeUTF(str);
        } else if (str.length() <= 1048576) {
            final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 65536) {
                out.writeByte(IOSupport.STR_DEF_U16);
                out.writeShort(bytes.length);
            } else {
                out.writeByte(IOSupport.STR_DEF_S32);
                out.writeInt(bytes.length);
            }
            out.write(bytes);
        } else {
            out.writeByte(IOSupport.STR_DEF_CHARS);
            out.writeInt(str.length());
            out.writeChars(str);
        }
    }

    private void writeStringRef(final int offset) throws IOException {
        if (offset <= 31) {
            out.writeByte(IOSupport.STR_REF_31 | offset & IOSupport.MASK_STR_LEN);
        } else if (offset <= 8223) {
            final int bits = offset - 32;
            out.writeByte(IOSupport.STR_REF_U8 | bits & IOSupport.MASK_STR_LEN);
            out.writeByte(bits >>> IOSupport.SHIFT_STR_LEN);
        } else if (offset <= 2105375) {
            final int bits = offset - 8224;
            out.writeByte(IOSupport.STR_REF_U16 | bits & IOSupport.MASK_STR_LEN);
            out.writeShort(bits >>> IOSupport.SHIFT_STR_LEN);
        } else {
            out.writeByte(IOSupport.STR_REF_S32);
            out.writeInt(offset);
        }
    }
}
