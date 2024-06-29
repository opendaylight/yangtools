/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.YangFeature;

/**
 * Run-time feature discovery service. Implementations of this interface are required to be registered with the
 * {@link ServiceLoader} framework.
 */
@NonNullByDefault
public interface YangFeatureProvider<R extends DataRoot<R>> {
    /**
     * Return the module this provider recognizes. It is implied that any feature defined in this module and not
     * advertized by any provider is unavailable.
     *
     * @return {@link DataRoot} class this provider binds to
     */
    Class<R> boundModule();

    /**
     * Return the set of supported features.
     *
     * @return Supported features.
     */
    Set<? extends YangFeature<?, R>> supportedFeatures();
}
