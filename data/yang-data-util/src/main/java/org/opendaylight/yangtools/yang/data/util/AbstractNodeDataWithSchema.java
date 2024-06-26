/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility abstract class for tracking parser state, as needed by StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 */
@Beta
public abstract sealed class AbstractNodeDataWithSchema<T extends DataSchemaNode>
        permits SimpleNodeDataWithSchema, CompositeNodeDataWithSchema {
    private final T schema;
    private ImmutableMap<QName, Object> attributes;

    AbstractNodeDataWithSchema(final T schema) {
        this.schema = requireNonNull(schema);
    }

    public static @NonNull AbstractNodeDataWithSchema<?> of(final DataSchemaNode schema) {
        return switch (schema) {
            case AnyxmlSchemaNode anyxml -> new AnyXmlNodeDataWithSchema(anyxml);
            case ContainerLike containerLike -> new ContainerNodeDataWithSchema(containerLike);
            case LeafSchemaNode leaf -> new LeafNodeDataWithSchema(leaf);
            case ListSchemaNode list -> new ListNodeDataWithSchema(list);
            case LeafListSchemaNode leafList -> new LeafListNodeDataWithSchema(leafList);
            case AnydataSchemaNode anydata -> new AnydataNodeDataWithSchema(anydata);
            default -> throw new IllegalStateException("Unsupported schema " + schema);
        };
    }

    /**
     * Return the associated schema node.
     *
     * @return Associated schema node.
     */
    public final @NonNull T getSchema() {
        return schema;
    }

    /**
     * Set the associated attributes.
     *
     * @param attributes parsed attributes
     */
    public final void setAttributes(final ImmutableMap<QName, Object> attributes) {
        checkState(this.attributes == null, "Node '%s' has already set its attributes to %s.", getSchema().getQName(),
                this.attributes);
        this.attributes = attributes;
    }

    /**
     * Return the associated attributes.
     *
     * @return associated attributes
     */
    public final ImmutableMap<QName, Object> getAttributes() {
        return attributes;
    }

    /**
     * Emit this node's events into the specified writer.
     *
     * @param writer Target writer
     * @throws IOException reported when thrown by the writer.
     */
    public final void write(final NormalizedNodeStreamWriter writer) throws IOException {
        write(writer, writer.extension(MetadataExtension.class));
    }

    protected abstract void write(NormalizedNodeStreamWriter writer, @Nullable MetadataExtension metaWriter)
        throws IOException;

    protected final NodeIdentifier provideNodeIdentifier() {
        return NodeIdentifier.create(schema.getQName());
    }

    protected final void writeMetadata(final MetadataExtension metaWriter) throws IOException {
        if (metaWriter != null && attributes != null && !attributes.isEmpty()) {
            metaWriter.metadata(attributes);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(schema);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final var other = (AbstractNodeDataWithSchema<?>) obj;
        return schema.equals(other.schema);
    }

}
