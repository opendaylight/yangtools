/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.IOException;

/**
 * SPI-level contract for implementations of {@link DataObjectSerializer}.
 * The contract is kept between implementation of {@link DataObjectSerializerRegistry},
 * which maintains the lookup context required for recursive serialization.
 */
/*
 * FIXME: this interface needs to be moved into .spi, but due to classpath funkyness
 *        of OSGi, that change has to be carefully orchestrated to ensure proper imports
 *        exist in all generated packages. One avenue how to achieve that is to move
 *        {@link YangModuleInfo} and modify code generator to add a static field
 *        to all generated classes which will point to the per-model YangModuleInfo
 *        (currently all users of it have to walk the package hierarchy, so that
 *        is an improvement in and of itself).
 */
public interface DataObjectSerializerImplementation {
    /**
     * Writes stream events for supplied data object to provided stream.
     *
     * <p>
     * DataObjectSerializerRegistry may be used to lookup serializers for other generated classes  in order to support
     * writing their events.
     */
    void serialize(DataObjectSerializerRegistry reg, DataObject obj, BindingStreamEventWriter stream)
            throws IOException;
}
