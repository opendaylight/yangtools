/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;

@Beta
public class AnydataNodeDataWithSchema extends SimpleNodeDataWithSchema<AnydataSchemaNode> {
    private Class<?> objectModel;

    public AnydataNodeDataWithSchema(final AnydataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    public AnydataNodeDataWithSchema(final AnydataSchemaNode dataSchemaNode, final Class<?> objectModel) {
        super(dataSchemaNode);
        this.objectModel = requireNonNull(objectModel);
    }

    @Override
    public Object getValue() {
        return getObjectModel().cast(super.getValue());
    }

    @Override
    public void setValue(final Object value) {
        final Class<?> clazz = getObjectModel();
        checkArgument(clazz.isInstance(value), "Value %s is not compatible with %s", clazz);
        super.setValue(value);
    }

    @Override
    protected void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter)
            throws IOException {
        writer.nextDataSchemaNode(getSchema());
        if (writer.startAnydataNode(provideNodeIdentifier(), getObjectModel())) {
            writer.scalarValue(getValue());
            writer.endNode();
        }
    }

    public final @NonNull Class<?> getObjectModel() {
        checkState(objectModel != null, "Object model not set");
        return objectModel;
    }

    public void setObjectModel(final Class<?> newObjectModel) {
        checkState(objectModel == null, "Object model already set to %s", objectModel);
        objectModel = requireNonNull(newObjectModel);
    }
}
