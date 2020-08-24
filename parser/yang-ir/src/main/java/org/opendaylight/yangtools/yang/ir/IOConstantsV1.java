/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

/**
 * Simplistic coding, without any real magic bit reuse. The idea is that each statement is in the form:
 * <pre>{@code HEADER LINE COLUMN KEYWORD [ARGUMENT] [SUBSTATEMENTS]}</pre>
 *
 * <p>
 * The {@code HEADER} is always a single byte, internally composed of four bitfields:
 * <pre>
 * +---+---+---+---+---+---+---+---+
 * |  KW TYPE  | SIZE  |ARG|  L6N  |
 * +---+---+---+---+---+---+---+---+
 * </pre>
 *
 * <p>
 * The {@code LINE} and {@code COLUMN} are variable size, indicated by the {@code L6N} bits:
 * <ul>
 *   <li>{@link #HDR_LOCATION_22} indicates u16 for LINE and u16 for COLUMN</li>
 *   <li>{@link #HDR_LOCATION_31} indicates u24 for LINE and u8 for COLUMN</li>
 *   <li>{@link #HDR_LOCATION_44} indicates s32 for LINE and s32 for COLUMN</li>
 * </ul>
 *
 * <p>
 * The {@code KEYWORD} is variable-format based on {@code KW TYPE} bits:
 * <ul>
 *   <li>{@link #HDR_KEY_DEF_QUAL} indicates a new definition, which is composed of two {@code STRING}s</li>
 *   <li>{@link #HDR_KEY_DEF_UQUAL} indicates a new definition, which is composed of a single {@code STRING}</li>
 *   <li>{@link #HDR_KEY_REF_U8} indicates a reference identified by a u8 integer</li>
 *   <li>{@link #HDR_KEY_REF_U16} indicates a reference identified by a u16 integer</li>
 *   <li>{@link #HDR_KEY_REF_S32} indicates a reference identified by a s32 integer</li>
 * </ul>
 * Once defined, each keyword can be referenced by encoding a reference with a linear counter of definition. I.e.
 * the first definition is {@code 0}, the second is {@code 1}, etc.
 *
 * <p>
 * The {@code ARGUMENT} is present only when indicated by {@link #HDR_ARGUMENT_PRESENT}. If it is present, it has
 * variable encoding in form
 * <pre>{@code ARGHDR [...]}</pre>
 */
final class IOConstantsV1 {
    // Statement indicator: indicates line/column split
    static final int HDR_LOCATION_22       = 0x01;
    static final int HDR_LOCATION_31       = 0x02;
    static final int HDR_LOCATION_44       = 0x03;
    static final int HDR_LOCATION_MASK     = HDR_LOCATION_44;
    // Argument presence
    static final int HDR_ARGUMENT_ABSENT   = 0x00;
    static final int HDR_ARGUMENT_PRESENT  = 0x04;
    static final int HDR_ARGUMENT_MASK     = HDR_ARGUMENT_PRESENT;
    // Child statement size
    static final int HDR_SIZE_0            = 0x00;
    static final int HDR_SIZE_U8           = 0x08;
    static final int HDR_SIZE_U16          = 0x10;
    static final int HDR_SIZE_S32          = 0x18;
    static final int HDR_SIZE_MASK         = HDR_SIZE_S32;
    // Keyword indication
    static final int HDR_KEY_DEF_UQUAL     = 0x00;
    static final int HDR_KEY_DEF_QUAL      = 0x20;
    // 0x40 reserved
    // 0x60 reserved
    // 0x80 reserved
    static final int HDR_KEY_REF_U8        = 0xA0;
    static final int HDR_KEY_REF_U16       = 0xC0;
    static final int HDR_KEY_REF_S32       = 0xE0;
    static final int HDR_KEY_MASK          = HDR_KEY_REF_S32;

    static final int ARG_TYPE_IDENTIFIER   = 0x01;
    static final int ARG_TYPE_DQUOT        = 0x02;
    static final int ARG_TYPE_SQUOT        = 0x03;
    static final int ARG_TYPE_UQUOT        = 0x04;
    static final int ARG_TYPE_CONCAT_U8    = 0x05;
    static final int ARG_TYPE_CONCAT_U16   = 0x06;
    static final int ARG_TYPE_CONCAT_S32   = 0x07;
    static final int ARG_TYPE_MASK         = ARG_TYPE_CONCAT_S32;

    static final int STR_DEF_UTF           = 0x00; // writeUTF(), <16384
    static final int STR_DEF_U8            = 0x10; // byte + UTF
    static final int STR_DEF_U16           = 0x20; // short + UTF
    static final int STR_DEF_S32           = 0x30; // int + UTF
    static final int STR_DEF_CHARS         = 0x40; // writeChars()
    static final int STR_REF_U8            = 0x50;
    static final int STR_REF_U16           = 0x60;
    static final int STR_REF_S32           = 0x70;
    static final int STR_MASK              = STR_REF_S32;

    private IOConstantsV1() {
        // Hidden on purpose
    }
}
