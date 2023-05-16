/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointChild;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Utility base class for implementing {@link MountPointChild} classes.
 */
@Beta
public abstract class AbstractMountPointChild implements MountPointChild {
    @Override
    public final NormalizedNode normalizeTo(final EffectiveModelContext schemaContext) throws IOException {
        final var result = new NormalizationResultHolder();
        final var streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        writeTo(streamWriter, MountPointContext.of(schemaContext));
        return result.getResult().data();
    }
}
