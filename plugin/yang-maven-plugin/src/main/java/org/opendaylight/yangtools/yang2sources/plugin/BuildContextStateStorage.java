/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A {@link StateStorage} based on {@link BuildContext}.
 */
@NonNullByDefault
final class BuildContextStateStorage extends StateStorage {
    private static final String STATE_KEY = YangToSourcesState.class.getName();

    private final BuildContext buildContext;

    BuildContextStateStorage(final BuildContext buildContext) {
        this.buildContext = requireNonNull(buildContext);
    }

    @Override
    @Nullable YangToSourcesState loadState() throws IOException {
        final var value = buildContext.getValue(STATE_KEY);
        if (value instanceof YangToSourcesState state) {
            return state;
        } else if (value == null) {
            return null;
        } else {
            throw new IOException("Unexpected loaded state " + value);
        }
    }

    @Override
    void storeState(final YangToSourcesState state) {
        buildContext.setValue(STATE_KEY, requireNonNull(state));
    }

    @Override
    void deleteState() {
        buildContext.setValue(STATE_KEY, null);
    }
}