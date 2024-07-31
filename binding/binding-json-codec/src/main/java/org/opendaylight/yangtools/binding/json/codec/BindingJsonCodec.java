/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.json.codec;

import static java.util.Objects.requireNonNull;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer.AugmentationResult;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer.NodeResult;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer.NormalizedResult;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple utility to writing out Java Binding constructors as a JSON snippet.
 */
@Component(service = BindingJsonCodec.class)
@NonNullByDefault
public final class BindingJsonCodec {
    private static final Logger LOG = LoggerFactory.getLogger(BindingJsonCodec.class);

    private final BindingNormalizedNodeSerializer serializer;
    private final JSONCodecFactory codecFactory;
    private final DataSchemaContextTree schemaTree;

    @Activate
    public BindingJsonCodec(@Reference final BindingDOMCodecServices dataCodec) {
        serializer = requireNonNull(dataCodec);
        final var modelContext = dataCodec.getRuntimeContext().modelContext();
        codecFactory = JSONCodecFactorySupplier.RFC7951.getShared(modelContext);
        schemaTree = DataSchemaContextTree.from(modelContext);
        LOG.debug("Binding/JSON codec started");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.debug("Binding/JSON codec stopped");
    }

    public <T extends DataObject> void writeDataObject(final Writer writer, final DataObjectReference<T> path,
            final T data) throws IOException {
        final NormalizedResult normalized;
        try {
            normalized = serializer.toNormalizedNode(path, data);
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to normalize data", e);
        }

        switch (normalized) {
            case AugmentationResult aug -> {
                // FIXME: implement this by starting/ending the top-level object and then writing out each of the
                //        children
                throw new UnsupportedOperationException("Augmentations not supported yet");
            }
            case NodeResult node -> {
                NormalizedNodeWriter.forStreamWriter(JSONNormalizedNodeStreamWriter.createExclusiveWriter(codecFactory,
                    inferPath(node), null, new JsonWriter(writer)))
                    .write(node.node())
                    .flush();
            }
        }
    }

    private Inference inferPath(final NormalizedResult normalized) throws IOException {
        return schemaTree.enterPath(normalized.path())
            .orElseThrow(cause -> new IOException("Failed to find path", cause))
            .stack().toInference();
    }
}
