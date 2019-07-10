/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchema;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Beta
@NonNullByDefault
public interface MountPointNodeFactory extends MountPointSchema {

    MountPointNode createMountPoint(ContainerNode delegate);
}
