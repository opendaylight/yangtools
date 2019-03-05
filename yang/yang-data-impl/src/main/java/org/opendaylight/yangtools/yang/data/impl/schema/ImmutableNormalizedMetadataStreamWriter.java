/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;

@Beta
public class ImmutableNormalizedMetadataStreamWriter extends ImmutableNormalizedNodeStreamWriter
        implements NormalizedMetadataStreamWriter {


    protected ImmutableNormalizedMetadataStreamWriter(final NormalizedNodeResult result) {
        super(result);
    }

    @Override
    public final ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(NormalizedMetadataStreamWriter.class, this);
    }

    @Override
    public final void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        // TODO Auto-generated method stub

    }
}
