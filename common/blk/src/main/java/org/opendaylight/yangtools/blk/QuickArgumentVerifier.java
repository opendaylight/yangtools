/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.blk;

import com.google.common.annotations.VisibleForTesting;

/**
 * The quick verifier: we just make sure there are no {@code null}s or empty strings.
 */
@VisibleForTesting
final class QuickArgumentVerifier extends ArgumentVerifier {
    QuickArgumentVerifier() {
        // Hidden on purpose
    }

    @Override
    void fullVerifyStr(final String arg) {
        // No-op
    }

    @Override
    void fullVerifyTxt(final String arg) {
        // no-op
    }
}