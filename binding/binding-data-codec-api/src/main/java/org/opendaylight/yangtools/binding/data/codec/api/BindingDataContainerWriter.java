/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * An entity capable of sourcing {@link NormalizedNodeStreamWriter} events from a {@link DataContainer}.
 *
 * @param <T> DataContainer type
 */
@Beta
public interface BindingDataContainerWriter<T extends DataContainer> {
    /**
     * Write a particular object to target writer.
     *
     * @param writer target writer
     * @param dataContainer DataContainer to write
     * @throws IOException when an I/O error occurs
     */
    void writeTo(NormalizedNodeStreamWriter writer, T dataContainer) throws IOException;
}
