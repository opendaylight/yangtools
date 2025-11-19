/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;

public final class YangDataContext extends AbstractCompositeContext {
    public YangDataContext(@NonNull YangDataSchemaNode schema) {
        super(null, schema, schema.toContainerLike());
    }
}
