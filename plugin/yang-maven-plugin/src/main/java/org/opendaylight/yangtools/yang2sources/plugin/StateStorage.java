/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * A storage engine able to store and load {@link YangToSourcesState}.
 */
@NonNullByDefault
abstract class StateStorage {
    private static final Logger LOG = LoggerFactory.getLogger(StateStorage.class);

    static StateStorage of(final BuildContext buildContext) {
        // FIXME: detect no-op BuildContext and fall back to a file
        LOG.debug("{} Using BuildContext to store state", YangToSourcesProcessor.LOG_PREFIX);
        return new BuildContextStateStorage(buildContext);
    }

    abstract @Nullable YangToSourcesState loadState() throws IOException;

    abstract void storeState(YangToSourcesState state) throws IOException;
}
