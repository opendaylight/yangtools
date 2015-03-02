/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base;

import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;

/**
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent.
 *
 * @deprecated Replaced with {@link EffectiveAugmentationSchema}.
 */
@Deprecated
public final class AugmentationSchemaProxy extends EffectiveAugmentationSchema {


    public AugmentationSchemaProxy(final AugmentationSchema augmentSchema, final Set<DataSchemaNode> realChildSchemas) {
       super(augmentSchema,realChildSchemas);
    }

}
