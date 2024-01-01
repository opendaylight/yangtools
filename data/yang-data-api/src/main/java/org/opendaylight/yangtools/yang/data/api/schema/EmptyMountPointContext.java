/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A simple {@link MountPointContext} which does not contain any mount points.
 */
record EmptyMountPointContext(@NonNull EffectiveModelContext modelContext) implements MountPointContext {
    EmptyMountPointContext {
        requireNonNull(modelContext);
    }

    @Override
    public Optional<MountPointContextFactory> findMountPoint(final MountPointLabel label) {
        return Optional.empty();
    }
}
