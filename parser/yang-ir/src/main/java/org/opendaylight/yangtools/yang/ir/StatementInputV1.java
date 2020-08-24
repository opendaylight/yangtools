/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import com.google.common.collect.ImmutableList;
import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;

final class StatementInputV1 extends StatementInput {
    private final List<@NonNull IRKeyword> keywords = new ArrayList<>();
    private final List<@NonNull String> strings = new ArrayList<>();

    StatementInputV1(final DataInput in) {
        super(in);
    }

    @Override
    IRStatement readStatement() throws IOException {
        final int header = in.readUnsignedByte();
        final int locationBits = header & IOConstantsV1.HDR_LOCATION_MASK;
        return switch (locationBits) {
            case IOConstantsV1.HDR_LOCATION_22 -> {
                final int startLine = in.readUnsignedShort();
                final int startColumn = in.readUnsignedShort();
                yield new IRStatement.Z22(readKeyword(header), readArgument(header), startLine, startColumn);
            }
            case IOConstantsV1.HDR_LOCATION_31 -> {
                final int value = in.readInt();
                yield new IRStatement.Z31(readKeyword(header), readArgument(header), value);
            }
            case IOConstantsV1.HDR_LOCATION_44 -> readStatement(header);
            default -> throw new IOException("Unhandled location " + Integer.toHexString(locationBits));
        };
    }

    private @NonNull IRStatement readStatement(final int header) throws IOException {
        final int startLine = in.readInt();
        final int startColumn = in.readInt();
        final var keyword = readKeyword(header);
        final var argument = readArgument(header);
        final var statements = readSubstatements(header);

        return switch (statements.size()) {
            case 0 -> new IRStatement.Z44(keyword, argument, startLine, startColumn);
            case 1 -> new IRStatement.O44(keyword, argument, statements.get(0), startLine, startColumn);
            default -> new IRStatement.L44(keyword, argument, statements, startLine, startColumn);
        };
    }

    private @NonNull IRKeyword readKeyword(final int header) throws IOException {
        final int keyBits = header & IOConstantsV1.HDR_KEY_MASK;
        return switch (keyBits) {
            case IOConstantsV1.HDR_KEY_REF_U8 -> lookupKeyword(in.readUnsignedByte());
            case IOConstantsV1.HDR_KEY_REF_U16 -> lookupKeyword(in.readUnsignedShort());
            case IOConstantsV1.HDR_KEY_REF_S32 -> lookupKeyword(in.readInt());
            case IOConstantsV1.HDR_KEY_DEF_QUAL -> defineKeyword(Qualified.of(readString(), readString()));
            case IOConstantsV1.HDR_KEY_DEF_UQUAL -> defineKeyword(Unqualified.of(readString()));
            default -> throw new IllegalStateException("Unhandled key " + Integer.toHexString(keyBits));
        };
    }

    private @NonNull IRKeyword lookupKeyword(final int code) throws IOException {
        try {
            return keywords.get(code);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Failed to look up keyword", e);
        }
    }

    private @NonNull IRKeyword defineKeyword(final @NonNull IRKeyword keyword) {
        keywords.add(keyword);
        return keyword;
    }

    private ImmutableList<IRStatement> readSubstatements(final int header) throws IOException {
        final int sizeBits = header & IOConstantsV1.HDR_SIZE_MASK;
        return switch (sizeBits) {
            case IOConstantsV1.HDR_SIZE_0 -> ImmutableList.of();
            case IOConstantsV1.HDR_SIZE_U8 -> readSubstatementList(in.readUnsignedByte());
            case IOConstantsV1.HDR_SIZE_U16 -> readSubstatementList(in.readUnsignedShort());
            case IOConstantsV1.HDR_SIZE_S32 -> readSubstatementList(in.readInt());
            default -> throw new IOException("Unhandled size " + Integer.toHexString(sizeBits));
        };
    }

    private ImmutableList<IRStatement> readSubstatementList(final int size) throws IOException {
        final var builder = ImmutableList.<IRStatement>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add(readStatement());
        }
        return builder.build();
    }

    private @Nullable IRArgument readArgument(final int header) throws IOException {
        if ((header & IOConstantsV1.HDR_ARGUMENT_MASK) == IOConstantsV1.HDR_ARGUMENT_ABSENT) {
            return null;
        }

        final int argHeader = in.readUnsignedByte();
        final int type = argHeader & IOConstantsV1.ARG_TYPE_MASK;
        return switch (type) {
            case IOConstantsV1.ARG_TYPE_IDENTIFIER -> IRArgument.identifier(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_DQUOT -> IRArgument.doubleQuoted(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_SQUOT -> IRArgument.singleQuoted(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_UQUOT -> IRArgument.unquoted(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_CONCAT_U8 -> readConcatenation(in.readUnsignedByte());
            case IOConstantsV1.ARG_TYPE_CONCAT_U16 -> readConcatenation(in.readUnsignedShort());
            case IOConstantsV1.ARG_TYPE_CONCAT_S32 -> readConcatenation(in.readInt());
            default -> throw new IOException("Unhandled argument " + Integer.toHexString(type));
        };
    }

    private @NonNull Single readSingleArgument() throws IOException {
        final int argHeader = in.readUnsignedByte();
        final int type = argHeader & IOConstantsV1.ARG_TYPE_MASK;
        return switch (type) {
            case IOConstantsV1.ARG_TYPE_IDENTIFIER -> IRArgument.identifier(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_DQUOT -> IRArgument.doubleQuoted(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_SQUOT -> IRArgument.singleQuoted(readString(argHeader));
            case IOConstantsV1.ARG_TYPE_UQUOT -> IRArgument.unquoted(readString(argHeader));
            default -> throw new IOException("Unhandled single argument " + Integer.toHexString(type));
        };
    }

    private @NonNull IRArgument readConcatenation(final int count) throws IOException {
        final var builder = ImmutableList.<Single>builderWithExpectedSize(count);
        for (int i = 0; i < count; ++i) {
            builder.add(readSingleArgument());
        }
        return IRArgument.of(builder.build());
    }

    private @NonNull String readString() throws IOException {
        return readString(in.readUnsignedByte());
    }

    private @NonNull String readString(final int header) throws IOException {
        final int type = header & IOConstantsV1.STR_MASK;
        return switch (type) {
            case IOConstantsV1.STR_DEF_UTF -> defineString(in.readUTF());
            case IOConstantsV1.STR_DEF_U8 -> defineString(readStringBytes(in.readUnsignedByte()));
            case IOConstantsV1.STR_DEF_U16 -> defineString(readStringBytes(in.readUnsignedShort()));
            case IOConstantsV1.STR_DEF_S32 -> defineString(readStringBytes(in.readInt()));
            case IOConstantsV1.STR_DEF_CHARS -> defineString(readStringChars());
            case IOConstantsV1.STR_REF_U8 -> lookupString(in.readUnsignedByte());
            case IOConstantsV1.STR_REF_U16 -> lookupString(in.readUnsignedShort());
            case IOConstantsV1.STR_REF_S32 -> lookupString(in.readInt());
            default -> throw new IOException("Unhandled string " + Integer.toHexString(type));
        };
    }

    private @NonNull String lookupString(final int offset) throws IOException {
        try {
            return strings.get(offset);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Invalid String reference " + offset, e);
        }
    }

    private @NonNull String defineString(final @NonNull String string) {
        strings.add(string);
        return string;
    }

    private @NonNull String readStringBytes(final int size) throws IOException {
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

    private @NonNull String readStringChars() throws IOException {
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
}
