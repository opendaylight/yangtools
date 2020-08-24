/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.ir.IRArgument.Concatenation;
import org.opendaylight.yangtools.yang.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Qualified;

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
            final int key = keyCode;
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
        if (stmt instanceof IRStatement.Z22) {
            writeHeader(keyBits, IOConstantsV1.HDR_LOCATION_22, sizeBits, argument);
            out.writeShort(stmt.startLine());
            out.writeShort(stmt.startColumn());
        } else if (stmt instanceof IRStatement.Z31 z31) {
            writeHeader(keyBits, IOConstantsV1.HDR_LOCATION_31, sizeBits, argument);
            out.writeInt(z31.value());
        } else {
            writeHeader(keyBits, IOConstantsV1.HDR_LOCATION_44, sizeBits, argument);
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
                writeString(keyword.identifier());
                keywords.put(keyword, keywords.size());
                break;
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

    private void writeString(final String str) throws IOException {
        final Integer key = strings.get(str);
        if (key != null) {
            writeStringRef(key);
        } else {
            writeStringDef(0, str);
        }
    }

    private void writeStringDef(final int bits, final String str) throws IOException {
        strings.put(str, strings.size());

        final int length = str.length();
        if (length <= Short.MAX_VALUE / 2) {
            out.writeByte(IOConstantsV1.STR_DEF_UTF | bits);
            out.writeUTF(str);
        } else if (length <= 1048576) {
            final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 65536) {
                out.writeByte(IOConstantsV1.STR_DEF_U16 | bits);
                out.writeShort(bytes.length);
            } else {
                out.writeByte(IOConstantsV1.STR_DEF_S32 | bits);
                out.writeInt(bytes.length);
            }
            out.write(bytes);
        } else {
            out.writeByte(IOConstantsV1.STR_DEF_CHARS | bits);
            out.writeInt(length);
            out.writeChars(str);
        }
    }

    private void writeStringRef(final int strCode) throws IOException {
        if (strCode <= 255) {
            out.writeByte(IOConstantsV1.STR_REF_U8);
            out.writeByte(strCode);
        } else if (strCode <= 65535) {
            out.writeByte(IOConstantsV1.STR_REF_U16);
            out.writeShort(strCode);
        } else {
            out.writeByte(IOConstantsV1.STR_REF_S32);
            out.writeInt(strCode);
        }
    }

    private void writeHeader(final int keyBits, final int locationBits, final int sizeBits, final IRArgument argument)
            throws IOException {
        final int argBits = argument != null ? IOConstantsV1.HDR_ARGUMENT_PRESENT : IOConstantsV1.HDR_ARGUMENT_ABSENT;
        out.writeByte(keyBits | sizeBits | argBits | locationBits);
    }

    private void writeArgument(final IRArgument argument) throws IOException {
        if (argument instanceof Single) {
            writeArgument((Single) argument);
        } else if (argument instanceof Concatenation) {
            writeArgument((Concatenation) argument);
        } else {
            throw new IllegalStateException("Unhandled argument " + argument);
        }
    }

    private void writeArgument(final Single argument) throws IOException {
        final int type;
        if (argument.isValidIdentifier()) {
            type = IOConstantsV1.ARG_TYPE_IDENTIFIER;
        } else if (argument.needQuoteCheck()) {
            type = IOConstantsV1.ARG_TYPE_UQUOT;
        } else if (argument.needUnescape()) {
            type = IOConstantsV1.ARG_TYPE_DQUOT;
        } else {
            type = IOConstantsV1.ARG_TYPE_SQUOT;
        }

        final String str = argument.string();
        final Integer existing = strings.get(str);
        if (existing != null) {
            final int strCode = existing;
            if (strCode <= 255) {
                out.writeByte(type | IOConstantsV1.STR_REF_U8);
                out.writeByte(strCode);
            } else if (strCode <= 65535) {
                out.writeByte(type | IOConstantsV1.STR_REF_U16);
                out.writeShort(strCode);
            } else {
                out.writeByte(type | IOConstantsV1.STR_REF_S32);
                out.writeInt(strCode);
            }
        } else {
            writeStringDef(type, str);
        }
    }

    private void writeArgument(final Concatenation argument) throws IOException {
        final List<? extends Single> parts = argument.parts();
        final int size = parts.size();
        if (size <= 255) {
            out.writeByte(IOConstantsV1.ARG_TYPE_CONCAT_U8);
            out.writeByte(size);
        } else if (size <= 65535) {
            out.writeByte(IOConstantsV1.ARG_TYPE_CONCAT_U16);
            out.writeShort(size);
        } else {
            out.writeByte(IOConstantsV1.ARG_TYPE_CONCAT_S32);
            out.writeInt(size);
        }

        for (Single part : parts) {
            writeArgument(part);
        }
    }
}
