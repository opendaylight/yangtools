/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

/**
 * Path Argument types used in Potassium encoding. These are encoded as a single byte, three bits of which are reserved
 * for PathArgument type itself:
 * <pre>
 *   7 6 5 4 3 2 1 0
 *  +-+-+-+-+-+-+-+-+
 *  |         | Type|
 *  +-+-+-+-+-+-+-+-+
 * </pre>
 * There are three type defined:
 * <ul>
 *   <li>{@link #NODE_IDENTIFIER}, which encodes a QName:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      |0 0 0| Q |0 0 1|
 *      +-+-+-+-+-+-+-+-+
 *     </pre>
 *   <li>{@link #NODE_IDENTIFIER_WITH_PREDICATES}, which encodes a QName same way NodeIdentifier does:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      | Size| Q |0 1 0|
 *      +-+-+-+-+-+-+-+-+
 *      </pre>
 *      but additionally encodes number of predicates contained using {@link #SIZE_0} through {@link #SIZE_4}. If that
 *      number cannot be expressed, {@link #SIZE_1B}, {@value #SIZE_2B} and {@link #SIZE_4B} indicate number and format
 *      of additional bytes that hold number of predicates.
 *
 *      <p>
 *      This is then followed by the specified number of QName/Object key/value pairs based on {@link PotassiumValue}
 *      encoding.
 *   </li>
 *   <li>{@link #NODE_WITH_VALUE}, which encodes a QName same way NodeIdentifier does:
 *     <pre>
 *       7 6 5 4 3 2 1 0
 *      +-+-+-+-+-+-+-+-+
 *      |0 0 0| Q |0 1 1|
 *      +-+-+-+-+-+-+-+-+
 *     </pre>
 *     but is additionally followed by a single encoded value, as per {@link PotassiumValue}.
 *   </li>
 *   </li>
 * </ul>
 */
final class PotassiumPathArgument {
    // 3 bits reserved for type...
    static final byte NODE_IDENTIFIER                 = 0x01;
    static final byte NODE_IDENTIFIER_WITH_PREDICATES = 0x02;
    static final byte NODE_WITH_VALUE                 = 0x03;

    // ... leaving five values currently unused
    // FIXME: this means we can use just two bits for type encoding
    // 0x00 reserved
    // 0x04 reserved
    // 0x05 reserved
    // 0x06 reserved
    // 0x07 reserved

    static final byte TYPE_MASK                       = 0x07;

    // For normal path path arguments we can either define a QName reference or follow a 1-4 byte reference.
    static final byte QNAME_DEF                       = 0x00;
    static final byte QNAME_REF_1B                    = 0x08; // Unsigned
    static final byte QNAME_REF_2B                    = 0x10; // Unsigned
    static final byte QNAME_REF_4B                    = 0x18; // Signed
    static final byte QNAME_MASK                      = QNAME_REF_4B;

    // For NodeIdentifierWithPredicates we also carry the number of subsequent path arguments. The case of 0-4 arguments
    // is indicated directly, otherwise there is 1-4 bytes carrying the reference.
    static final byte SIZE_0                          = 0x00;
    static final byte SIZE_1                          = 0x20;
    static final byte SIZE_2                          = 0x40;
    static final byte SIZE_3                          = 0x60;
    static final byte SIZE_4                          = (byte) 0x80;
    static final byte SIZE_1B                         = (byte) 0xA0;
    static final byte SIZE_2B                         = (byte) 0xC0;
    static final byte SIZE_4B                         = (byte) 0xE0;
    static final byte SIZE_MASK                       = SIZE_4B;
    static final byte SIZE_SHIFT                      = 5;

    private PotassiumPathArgument() {

    }
}
