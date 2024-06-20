/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.UnsignedLong;
import java.util.Dictionary;
import java.util.Map;
import org.opendaylight.yangtools.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.binding.runtime.osgi.ModelGenerationAware;
import org.opendaylight.yangtools.binding.runtime.osgi.OSGiModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(factory = OSGiModuleInfoSnapshotImpl.FACTORY_NAME, service = OSGiModuleInfoSnapshot.class)
public final class OSGiModuleInfoSnapshotImpl implements OSGiModuleInfoSnapshot {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.dom.schema.osgi.impl.OSGiEffectiveModelImpl";

    // Keys to for activation properties
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.dom.schema.osgi.impl.Generation";
    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.dom.schema.osgi.impl.ModuleInfoSnapshot";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiModuleInfoSnapshotImpl.class);

    private final ModuleInfoSnapshot delegate;
    private final Uint64 generation;

    @Activate
    public OSGiModuleInfoSnapshotImpl(final Map<String, ?> properties) {
        generation = (Uint64) verifyNotNull(properties.get(GENERATION));
        delegate = (ModuleInfoSnapshot) verifyNotNull(properties.get(DELEGATE));
        LOG.info("EffectiveModelContext generation {} activated", generation);
    }

    @Override
    public Uint64 generation() {
        return generation;
    }

    @Override
    public ModuleInfoSnapshot service() {
        return delegate;
    }

    @Deactivate
    void deactivate() {
        LOG.info("EffectiveModelContext generation {} deactivated", generation);
    }

    static Dictionary<String, ?> props(final long generation, final ModuleInfoSnapshot delegate) {
        return FrameworkUtil.asDictionary(Map.of(
            Constants.SERVICE_RANKING, ModelGenerationAware.computeServiceRanking(generation),
            GENERATION, UnsignedLong.fromLongBits(generation),
            DELEGATE, delegate));
    }
}
