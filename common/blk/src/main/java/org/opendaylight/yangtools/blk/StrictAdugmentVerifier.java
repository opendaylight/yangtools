/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.blk;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;

/**
 * The strict verifier: we do full argument checks.
 */
@VisibleForTesting
final class StrictAdugmentVerifier extends ArgumentVerifier {
    StrictAdugmentVerifier() {
        // Hidden on purpose
    }

    @Override
    void fullVerifyStr(final String arg) {
        final var nl = arg.indexOf('\n');
        if (nl != -1) {
            throw new VerifyException("newline at offset " + nl + " of '" + arg + "'");
        }
    }

    @Override
    void fullVerifyTxt(final String arg) {
        final var nl = arg.lastIndexOf('\n');
        if (nl == -1) {
            throw new VerifyException("no newline in '" + arg + "'");
        }
        final var tail = nl + 1;
        if (tail != arg.length()) {
            throw new VerifyException("trailing text fragment " + arg.substring(tail));
        }
    }
}
