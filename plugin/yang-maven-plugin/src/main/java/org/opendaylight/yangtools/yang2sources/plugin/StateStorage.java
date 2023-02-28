/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import java.io.IOException;
import java.nio.file.Path;
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
    private static final String DUMMY_KEY = StateStorage.class.getName();
    private static final Object DUMMY_VALUE = new Object();

    static StateStorage of(final BuildContext buildContext, final Path fallbackFile) {
        // Check if BuildContext works. If it does, we use it, otherwise we use fallback to the specified directory
        buildContext.setValue(DUMMY_KEY, DUMMY_VALUE);
        if (DUMMY_VALUE.equals(buildContext.getValue(DUMMY_KEY))) {
            buildContext.setValue(DUMMY_KEY, null);
            LOG.debug("{} Using BuildContext to store state", YangToSourcesProcessor.LOG_PREFIX);
            return new BuildContextStateStorage(buildContext);
        }

        LOG.debug("{} Using {} to store state", YangToSourcesProcessor.LOG_PREFIX, fallbackFile);
        return new FileStateStorage(fallbackFile);
    }

    abstract @Nullable YangToSourcesState loadState() throws IOException;

    abstract void storeState(YangToSourcesState state) throws IOException;
}
