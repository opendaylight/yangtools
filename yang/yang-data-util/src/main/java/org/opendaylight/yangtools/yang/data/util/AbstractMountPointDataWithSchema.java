/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * A {@link CompositeNodeDataWithSchema} which can hold mount-point data. This data is manipulated through
 * {@link #getMountPointData(QName)}.
 */
@Beta
public abstract class AbstractMountPointDataWithSchema<T extends DataSchemaNode>
        extends CompositeNodeDataWithSchema<T> {
    private MountPointData mountedData;

    AbstractMountPointDataWithSchema(final T schema) {
        super(schema);
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final NormalizedMetadataStreamWriter metaWriter)
            throws IOException {
        super.write(writer, metaWriter);
        if (mountedData != null) {
            mountedData.write(writer);
        }
    }

    public final MountPointData getMountPointData(final QName label) {
        if (mountedData != null) {
            final QName existing = mountedData.getIdentifier();
            checkState(label.equals(existing), "Mismatched mount label {}, already have {}", label, existing);
        } else {
            mountedData = new MountPointData(label);
        }
        return mountedData;
    }
}
