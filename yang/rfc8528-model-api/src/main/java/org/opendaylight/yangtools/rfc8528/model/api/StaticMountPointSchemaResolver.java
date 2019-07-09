/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A resolver which has static knowledge of the SchemaContext which should be used to interpret mount point data.
 * Instances of this interface should be used in contexts where the mount point data is expected not to contain
 * required {@code ietf-yang-library} data, for example due to filtering.
 */
@Beta
@NonNullByDefault
public interface StaticMountPointSchemaResolver extends MountPointSchemaResolver {

    MountPointSchema getSchema();
}
