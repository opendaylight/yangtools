/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.UnsignedLong;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.opendaylight.mdsal.binding.dom.codec.osgi.OSGiBindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Factory Component which implements {@link OSGiBindingDOMCodecServices}.
 */
@Beta
@Component(factory = OSGiBindingDOMCodecServicesImpl.FACTORY_NAME, service = OSGiBindingDOMCodecServices.class)
public final class OSGiBindingDOMCodecServicesImpl implements OSGiBindingDOMCodecServices {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME =
            "org.opendaylight.mdsal.binding.dom.codec.osgi.impl.OSGiBindingDOMCodecServicesImpl";

    // Keys to for activation properties
    @VisibleForTesting
    static final String GENERATION = "org.opendaylight.mdsal.binding.dom.codec.osgi.impl.Generation";
    @VisibleForTesting
    static final String DELEGATE = "org.opendaylight.mdsal.binding.dom.codec.osgi.impl.BindingDOMCodecServices";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingDOMCodecServicesImpl.class);

    private BindingDOMCodecServices delegate;
    private UnsignedLong generation;

    @Override
    public UnsignedLong getGeneration() {
        return verifyNotNull(generation);
    }

    @Override
    public BindingDOMCodecServices getService() {
        return verifyNotNull(delegate);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        generation = (UnsignedLong) verifyNotNull(properties.get(GENERATION));
        delegate = (BindingDOMCodecServices) verifyNotNull(properties.get(DELEGATE));
        LOG.info("Binding/DOM Codec generation {} activated", generation);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Binding/DOM Codec generation {} deactivated", generation);
    }

    @SuppressModernizer
    static Dictionary<String, ?> props(final @NonNull UnsignedLong generation, final @NonNull Integer ranking,
            final BindingDOMCodecServices delegate) {
        final Dictionary<String, Object> ret = new Hashtable<>(4);
        ret.put(Constants.SERVICE_RANKING, ranking);
        ret.put(GENERATION, generation);
        ret.put(DELEGATE, requireNonNull(delegate));
        return ret;
    }
}
