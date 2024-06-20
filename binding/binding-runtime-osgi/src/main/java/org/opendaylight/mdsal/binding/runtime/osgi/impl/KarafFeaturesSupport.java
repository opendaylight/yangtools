/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import org.apache.karaf.features.FeaturesService;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optional support for Karaf's FeaturesService. This class centralizes wrapping based on bundle resolution state. If
 * FeaturesService interface is not resolved, this class ends up reusing RegularYangModuleInfoRegistry. If the interface
 * is resolved, we use it to locate the appropriate service whenever we are asked to activate.
 */
@NonNullByDefault
final class KarafFeaturesSupport {
    @FunctionalInterface
    private interface WrapperFunction {
        YangModuleInfoRegistry wrap(BundleContext ctx, RegularYangModuleInfoRegistry delegate);
    }

    private static final class NoopWrapperFunction implements WrapperFunction {
        @Override
        public YangModuleInfoRegistry wrap(final BundleContext ctx, final RegularYangModuleInfoRegistry delegate) {
            return delegate;
        }
    }

    private static final class KarafWrapperFunction implements WrapperFunction {
        // Forces FeaturesService to be resolved
        private static final Class<FeaturesService> FEATURES_SERVICE = FeaturesService.class;

        @Override
        public YangModuleInfoRegistry wrap(final BundleContext ctx, final RegularYangModuleInfoRegistry delegate) {
            final ServiceReference<FeaturesService> ref = ctx.getServiceReference(FEATURES_SERVICE);
            if (ref != null) {
                final FeaturesService features = ctx.getService(ref);
                if (features != null) {
                    LOG.debug("Integrating with Karaf's FeaturesService");
                    return KarafYangModuleInfoRegistry.create(features, delegate);
                }
            }

            return delegate;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(KarafFeaturesSupport.class);
    private static final WrapperFunction WRAPPER = staticInit();

    private KarafFeaturesSupport() {
        // Hidden on purpose
    }

    static YangModuleInfoRegistry wrap(final BundleContext ctx, final RegularYangModuleInfoRegistry regular) {
        return WRAPPER.wrap(ctx, regular);
    }

    private static WrapperFunction staticInit() {
        try {
            final WrapperFunction karaf = new KarafWrapperFunction();
            LOG.info("Will attempt to integrate with Karaf FeaturesService");
            return karaf;
        } catch (NoClassDefFoundError e) {
            LOG.trace("Failed to initialize Karaf support", e);
            LOG.info("Karaf FeaturesService integration disabled");
            return new NoopWrapperFunction();
        }
    }
}
