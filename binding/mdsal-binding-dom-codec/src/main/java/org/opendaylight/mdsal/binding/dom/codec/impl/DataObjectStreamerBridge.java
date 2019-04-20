/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Bridge for initializing {@link DataObjectStreamer} instance constants during class loading time. This class is public
 * only due to implementation restrictions and can change at any time.
 */
// FIXME: this bridge is only necessary to work around Javassist compiler resolution of dependencies. If we switch to
//         a more bytecode-oriented framework, this bridge becomes superfluous.
//
@Beta
public final class DataObjectStreamerBridge {
    private static final ThreadLocal<DataObjectStreamerCustomizer> CURRENT_CUSTOMIZER = new ThreadLocal<>();

    private DataObjectStreamerBridge() {

    }

    public static @NonNull DataObjectStreamer<?> resolve(final @NonNull String methodName) {
        return verifyNotNull(CURRENT_CUSTOMIZER.get(), "No customizer attached").resolve(methodName);
    }

    static @Nullable DataObjectStreamerCustomizer setup(final @NonNull DataObjectStreamerCustomizer next) {
        final DataObjectStreamerCustomizer prev = CURRENT_CUSTOMIZER.get();
        CURRENT_CUSTOMIZER.set(verifyNotNull(next));
        return prev;
    }

    static void tearDown(final @Nullable DataObjectStreamerCustomizer prev) {
        if (prev == null) {
            CURRENT_CUSTOMIZER.remove();
        } else {
            CURRENT_CUSTOMIZER.set(prev);
        }
    }
}
