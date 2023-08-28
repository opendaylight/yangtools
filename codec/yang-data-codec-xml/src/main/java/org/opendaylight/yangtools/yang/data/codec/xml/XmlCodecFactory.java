/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.util.EmptyMountPointContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A thread-safe factory for instantiating XML codecs.
 */
@Beta
public final class XmlCodecFactory extends CodecFactory {
    private XmlCodecFactory(final MountPointContext mountCtx) {
        super(mountCtx);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context MountPointContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final MountPointContext context) {
        return new XmlCodecFactory(context);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final EffectiveModelContext context) {
        return create(new EmptyMountPointContext(context));
    }
}
