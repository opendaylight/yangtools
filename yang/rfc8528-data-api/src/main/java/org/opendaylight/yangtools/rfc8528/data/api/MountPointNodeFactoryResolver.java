/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;

/**
 * An entity able to resolve the SchemaContext for embedded mount points.
 */
@Beta
public interface MountPointNodeFactoryResolver {

    @NonNull MountPointNodeFactory resolveMountPoint(@NonNull Map<ContainerName, MountPointChild> libraryContainers,
            @Nullable MountPointChild schemaMounts) throws YangParserException;

}
