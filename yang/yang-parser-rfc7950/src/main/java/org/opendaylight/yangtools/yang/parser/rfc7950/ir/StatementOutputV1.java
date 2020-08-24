/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Qualified;

final class StatementOutputV1 extends StatementOutput {
    private final Map<IRKeyword, Integer> keywords = new HashMap<>();
    private final Map<String, Integer> strings = new HashMap<>();

    StatementOutputV1(final DataOutput out) {
        super(out);
    }

    @Override
    void writeStatement(final IRStatement stmt) throws IOException {
        final List<? extends IRStatement> statements = stmt.statements();
        final int size = statements.size();
        final int sizeBits;
        if (size == 0) {
            sizeBits = IOConstantsV1.HDR_SIZE_0;
        } else if (size <= 255) {
            sizeBits = IOConstantsV1.HDR_SIZE_U8;
        } else if (size <= 65535) {
            sizeBits = IOConstantsV1.HDR_SIZE_U16;
        } else {
            sizeBits = IOConstantsV1.HDR_SIZE_S32;
        }

        final IRKeyword keyword = stmt.keyword();
        final Integer keyCode = keywords.get(keyword);
        final int keyBits;
        if (keyCode != null) {
            final int key = keyCode.intValue();
            if (key <= 255) {
                keyBits = IOConstantsV1.HDR_KEY_REF_U8;
            } else if (size <= 65535) {
                keyBits = IOConstantsV1.HDR_KEY_REF_U16;
            } else {
                keyBits = IOConstantsV1.HDR_KEY_REF_S32;
            }
        } else {
            keyBits = keyword instanceof Qualified ? IOConstantsV1.HDR_KEY_DEF_QUAL : IOConstantsV1.HDR_KEY_DEF_UQUAL;
        }

        final IRArgument argument = stmt.argument();
        if (stmt instanceof IRStatement022) {
            writeHeader(keyCode, IOConstantsV1.HDR_LOCATION_22, sizeBits, argument);
            out.writeShort(stmt.startLine());
            out.writeShort(stmt.startColumn());
        } else if (stmt instanceof IRStatement031) {
            writeHeader(keyBits, IOConstantsV1.HDR_LOCATION_31, sizeBits, argument);
            out.writeInt(((IRStatement031) stmt).value());
        } else {
            writeHeader(keyCode, IOConstantsV1.HDR_LOCATION_44, sizeBits, argument);
            out.writeInt(stmt.startLine());
            out.writeInt(stmt.startColumn());
        }

        switch (keyBits) {
            case IOConstantsV1.HDR_KEY_REF_U8:
                out.writeByte(keyCode);
                break;
            case IOConstantsV1.HDR_KEY_REF_U16:
                out.writeShort(keyCode);
                break;
            case IOConstantsV1.HDR_KEY_REF_S32:
                out.writeInt(keyCode);
                break;
            case IOConstantsV1.HDR_KEY_DEF_QUAL:
                writeString(keyword.prefix());
                // Fall-through
            case IOConstantsV1.HDR_KEY_DEF_UQUAL:
                writeString(keyword.identifier());
                keywords.put(keyword, keywords.size());
                break;
            default:
                throw new IllegalStateException("Unhandled key bits " + keyBits);

        }

        if (argument != null) {
            writeArgument(argument);
        }

        switch (sizeBits) {
            case IOConstantsV1.HDR_SIZE_0:
                // All done
                return;
            case IOConstantsV1.HDR_SIZE_U8:
                out.writeByte(statements.size());
                break;
            case IOConstantsV1.HDR_SIZE_U16:
                out.writeShort(size);
                break;
            case IOConstantsV1.HDR_SIZE_S32:
                out.writeInt(size);
                break;
            default:
                throw new IllegalStateException("Unhandled size bits " + sizeBits);
        }

        for (IRStatement child : statements) {
            writeStatement(child);
        }
    }

    private void writeString(final String str) {
        // TODO Auto-generated method stub

    }

    private void writeHeader(final int keyBits, final int locationBits, final int sizeBits, final IRArgument argument)
            throws IOException {
        final int argBits = argument != null ? IOConstantsV1.HDR_ARGUMENT_PRESENT : IOConstantsV1.HDR_ARGUMENT_ABSENT;
        out.writeByte(keyBits | sizeBits | argBits | locationBits);
    }

    private void writeArgument(final IRArgument argument) throws IOException {
        // TODO Auto-generated method stub

    }


//        final IRKeyword keyword = stmt.keyword();
//        final byte key = keyword.ioType();
//
//        out.writeByte(stmt | arg | key);
//        switch (key) {
//            case IOSupport.KEY_QUAL:
//                writeString(verifyNotNull(keyword.prefix()));
//                writeString(keyword.identifier());
//                break;
//            case IOSupport.KEY_UQUAL:
//                writeString(keyword.identifier());
//                break;
//            default:
//                throw new IOException("Internal error: unhandled key " + key);
//        }
//
//        switch (arg) {
//            case IOSupport.ARG_NONE:
//                // No-op
//                break;
//            case IOSupport.ARG_IDENTIFIER:
//            case IOSupport.ARG_DQUOT:
//            case IOSupport.ARG_SQUOT:
//            case IOSupport.ARG_UQUOT:
//                writeString(((Single) argument).string());
//                break;
//            case IOSupport.ARG_CONCAT:
//            case IOSupport.ARG_CONCAT_U8:
//                // FIXME: implement this
//            default:
//                throw new IOException("Internal error: unhandled argument " + arg);
//        }
//
//        final List<? extends IRStatement> statements = stmt.statements();
//        switch (stmt) {
//            case IOSupport.STMT_022:
//                out.writeShort(stmt.startLine());
//                out.writeShort(stmt.startColumn());
//                break;
//            case IOSupport.STMT_031:
//                out.writeInt(((IRStatement031) stmt).value());
//                break;
//            case IOSupport.STMT_044:
//                writeLocation(stmt);
//                break;
//            case IOSupport.STMT_144:
//                writeLocation(stmt);
//                writeStatement(statements.get(0));
//                break;
//            case IOSupport.STMT_N44_U8:
//                writeLocation(stmt);
//                out.writeByte(statements.size());
//                writeStatements(statements);
//                break;
//            case IOSupport.STMT_N44_U16:
//                writeLocation(stmt);
//                out.writeShort(statements.size());
//                writeStatements(statements);
//                break;
//            case IOSupport.STMT_N44_S32:
//                writeLocation(stmt);
//                out.writeInt(statements.size());
//                writeStatements(statements);
//                break;
//            default:
//                throw new IOException("Internal error: unhandled statement " + stmt);
//        }


//    private void writeString(final @NonNull String str) throws IOException {
//        final Integer key = strings.get(str);
//        if (key != null) {
//            writeStringRef(key.intValue());
//        } else {
//            writeStringDef(str);
//        }
//    }
//
//    private void writeStringDef(final @NonNull String str) throws IOException {
//        strings.put(str, strings.size());
//
//        if (str.length() <= Short.MAX_VALUE / 2) {
//            out.writeByte(IOSupport.STR_DEF_UTF);
//            out.writeUTF(str);
//        } else if (str.length() <= 1048576) {
//            final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
//            if (bytes.length < 65536) {
//                out.writeByte(IOSupport.STR_DEF_U16);
//                out.writeShort(bytes.length);
//            } else {
//                out.writeByte(IOSupport.STR_DEF_S32);
//                out.writeInt(bytes.length);
//            }
//            out.write(bytes);
//        } else {
//            out.writeByte(IOSupport.STR_DEF_CHARS);
//            out.writeInt(str.length());
//            out.writeChars(str);
//        }
//    }
//
//    private void writeStringRef(final int offset) throws IOException {
//        if (offset <= 31) {
//            out.writeByte(IOSupport.STR_REF_31 | offset & IOSupport.MASK_STR_LEN);
//        } else if (offset <= 8223) {
//            final int bits = offset - 32;
//            out.writeByte(IOSupport.STR_REF_U8 | bits & IOSupport.MASK_STR_LEN);
//            out.writeByte(bits >>> IOSupport.SHIFT_STR_LEN);
//        } else if (offset <= 2105375) {
//            final int bits = offset - 8224;
//            out.writeByte(IOSupport.STR_REF_U16 | bits & IOSupport.MASK_STR_LEN);
//            out.writeShort(bits >>> IOSupport.SHIFT_STR_LEN);
//        } else {
//            out.writeByte(IOSupport.STR_REF_S32);
//            out.writeInt(offset);
//        }
//    }
}
