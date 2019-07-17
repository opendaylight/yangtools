/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rcf8528.data.util;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.AbstractSchemaContextProvider;

/**
 * A simple {@link MountPointContext} which does not contain any mount points.
 */
@Beta
public final class EmptyMountPointContext extends AbstractSchemaContextProvider implements MountPointContext {
    public EmptyMountPointContext(final SchemaContext schemaContext) {
        super(schemaContext);
    }

    @Override
    public Optional<MountPointContextFactory> findMountPoint(final MountPointIdentifier label) {
        return Optional.empty();
    }
}
