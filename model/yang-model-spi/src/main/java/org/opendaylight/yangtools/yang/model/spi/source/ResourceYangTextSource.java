/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import com.google.common.io.Resources;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A resource-backed {@link YangTextSource}.
 */
public final class ResourceYangTextSource extends URLYangTextSource {
    /**
     * Default constructor.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @param charset Expected character set
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public ResourceYangTextSource(final Class<?> clazz, final String resourceName, final Charset charset) {
        super(SourceIdentifier.ofYangFileName(resourceName.substring(resourceName.lastIndexOf('/') + 1)),
            Resources.getResource(clazz, resourceName), charset);
    }

    /**
     * Constructor using {@link StandardCharsets#UTF_8} character set.
     *
     * @param clazz Class reference
     * @param resourceName Resource name
     * @throws IllegalArgumentException if the resource does not exist or if the name has invalid format
     */
    public ResourceYangTextSource(final Class<?> clazz, final String resourceName) {
        this(clazz, resourceName, StandardCharsets.UTF_8);
    }
}
