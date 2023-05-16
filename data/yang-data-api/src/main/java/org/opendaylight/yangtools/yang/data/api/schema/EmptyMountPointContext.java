/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Optional;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.spi.AbstractEffectiveModelContextProvider;

/**
 * A simple {@link MountPointContext} which does not contain any mount points.
 */
final class EmptyMountPointContext extends AbstractEffectiveModelContextProvider implements MountPointContext {
    EmptyMountPointContext(final EffectiveModelContext modelContext) {
        super(modelContext);
    }

    @Override
    public Optional<MountPointContextFactory> findMountPoint(final MountPointLabel label) {
        return Optional.empty();
    }
}
