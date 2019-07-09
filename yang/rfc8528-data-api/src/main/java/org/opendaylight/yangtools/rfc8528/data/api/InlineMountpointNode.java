/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * A mount point with {@code schema-ref} inline, as defined in RFC8528.
 */
@Beta
@NonNullByDefault
public interface InlineMountpointNode extends MountPointNode, SchemaContextProvider {
    @Override
    // FIXME: remove this override when SchemaContextProvider's method has sane semantics.
    @NonNull SchemaContext getSchemaContext();
}
