/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.gen.impl;

import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;

/**
 * @deprecated Use {@link org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator} instead.
 */
@Deprecated
public class StreamWriterGenerator {
    public static DataObjectSerializerGenerator create(final JavassistUtils utils) {
        return org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator.create(utils);
    }
}
