/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlCodecFactory;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * An immutable context holding a consistent view of things related to data bind operations.
 */
public final class DatabindContext {
    private static final VarHandle JSON_CODECS;
    private static final VarHandle XML_CODECS;
    private static final VarHandle SCHEMA_TREE;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            JSON_CODECS = lookup.findVarHandle(DatabindContext.class, "jsonCodecs", JSONCodecFactory.class);
            XML_CODECS = lookup.findVarHandle(DatabindContext.class, "xmlCodecs", XmlCodecFactory.class);
            SCHEMA_TREE = lookup.findVarHandle(DatabindContext.class, "schemaTree", DataSchemaContextTree.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull MountPointContext mountContext;

    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile DataSchemaContextTree schemaTree;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile JSONCodecFactory jsonCodecs;
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile XmlCodecFactory xmlCodecs;

    private DatabindContext(final @NonNull MountPointContext mountContext) {
        this.mountContext = requireNonNull(mountContext);
    }

    public static @NonNull DatabindContext ofModel(final EffectiveModelContext modelContext) {
        return ofMountPoint(MountPointContext.of(modelContext));
    }

    public static @NonNull DatabindContext ofMountPoint(final MountPointContext mountContext) {
        return new DatabindContext(mountContext);
    }

    public @NonNull EffectiveModelContext modelContext() {
        return mountContext.modelContext();
    }

    public @NonNull DataSchemaContextTree schemaTree() {
        final var existing = (DataSchemaContextTree) SCHEMA_TREE.getAcquire(this);
        return existing != null ? existing : createSchemaTree();
    }

    private @NonNull DataSchemaContextTree createSchemaTree() {
        final var created = DataSchemaContextTree.from(modelContext());
        final var witness = (DataSchemaContextTree) SCHEMA_TREE.compareAndExchangeRelease(this, null, created);
        return witness != null ? witness : created;
    }

    public @NonNull JSONCodecFactory jsonCodecs() {
        final var existing = (JSONCodecFactory) JSON_CODECS.getAcquire(this);
        return existing != null ? existing : createJsonCodecs();
    }

    private @NonNull JSONCodecFactory createJsonCodecs() {
        final var created = JSONCodecFactorySupplier.RFC7951.getShared(mountContext.modelContext());
        final var witness = (JSONCodecFactory) JSON_CODECS.compareAndExchangeRelease(this, null, created);
        return witness != null ? witness : created;
    }

    public @NonNull XmlCodecFactory xmlCodecs() {
        final var existing = (XmlCodecFactory) XML_CODECS.getAcquire(this);
        return existing != null ? existing : createXmlCodecs();
    }

    private @NonNull XmlCodecFactory createXmlCodecs() {
        final var created = XmlCodecFactory.create(mountContext);
        final var witness = (XmlCodecFactory) XML_CODECS.compareAndExchangeRelease(this, null, created);
        return witness != null ? witness : created;
    }
}
