/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * API entry point for acquiring {@link AbstractJSONCodecFactory} instances.
 */
public interface JSONCodecFactorySupplier {
    @NonNull AbstractJSONCodecFactory getPrecomputed(@NonNull SchemaContext context);

    @NonNull Optional<? extends AbstractJSONCodecFactory> getPrecomputedIfAvailable(@NonNull SchemaContext context);

    @NonNull AbstractJSONCodecFactory getShared(@NonNull SchemaContext context);

    @NonNull AbstractJSONCodecFactory createLazy(@NonNull SchemaContext context);

    @NonNull AbstractJSONCodecFactory createSimple(@NonNull SchemaContext context);
}
