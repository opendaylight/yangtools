/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

final class TokenTypes {
    static final byte SIGNATURE_MARKER = (byte) 0xab;

    // Original stream version. Uses a per-stream dictionary for strings. QNames are serialized as three strings.
    // LITHIUM_VERSION = 1;
    //
    // Revised stream version. Unlike {@link #LITHIUM_VERSION}, QNames and QNameModules are using a per-stream
    // dictionary, too.
    // NEON_SR2_VERSION = 2;
    //
    // From-scratch designed version shipping in Sodium SR1.
    // SODIUM_SR1_VERSION = 3;

    /**
     * Magnesium version. Structurally matches {@link #SODIUM_SR1_VERSION}, but does not allow BigIntegers to be
     * present.
     */
    static final short MAGNESIUM_VERSION = 4;
    /**
     * Potassium version. Breaks compatibility from Magnesium in terms of NormalizedNode structure, as AugmentationNodes
     * and AugmentationIdentifiers are not preserved.
     */
    static final short POTASSIUM_VERSION = 5;

    private TokenTypes() {
        // Utility class
    }
}
