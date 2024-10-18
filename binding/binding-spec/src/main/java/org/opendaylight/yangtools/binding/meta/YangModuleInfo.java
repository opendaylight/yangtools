/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.meta;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Information and model capture for Binding V1. Instances of this class identify a packaged model and allow access
 * to its YANG text. They also contain references to {@link YangModuleInfo} instances as observed at code generation
 * time.
 *
 * <p>The purpose of this class is to ensure package resolution order in OSGi environments, as implementations of this
 * interface are required to be co-located with generated code. When this module relies on some imports, that dependency
 * is expressed across jars via an implementation requirement to reference YangModuleInfos.
 */
@NonNullByDefault
public interface YangModuleInfo extends Immutable {
    /**
     * Returns YANG module name, as a composite {@link QName}. Module's namespace and revision maps to
     * {@link QName#getModule()} and module name maps to {@link QName#getLocalName()}.
     *
     * @return YANG module name.
     */
    QName getName();

    /**
     * Return an open stream containing YANG text for this module. The stream is required to be UTF-8 encoded.
     *
     * @return An open stream.
     * @throws IOException If the stream cannot be opened.
     */
    InputStream openYangTextStream() throws IOException;

    default InputStream openYodlStream() throws IOException {
        throw new IOException("YODL not supported");
    }

    /**
     * Return {@link YangModuleInfo} objects for all modules which are imported by this module. Default implementation
     * returns an empty list.
     *
     * @return {@link YangModuleInfo} objects of all imported modules.
     */
    default Collection<YangModuleInfo> getImportedModules() {
        return ImmutableList.of();
    }

    /**
     * Return a {@link ByteSource} accessing the YANG text of the module.
     *
     * @return A ByteSource.
     */
    default ByteSource getYangTextByteSource() {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return openYangTextStream();
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this).add("name", getName()).toString();
            }
        };
    }

    /**
     * Return a {@link CharSource} accessing the YANG text of the module.
     *
     * @return A CharSource.
     */
    default CharSource getYangTextCharSource() {
        return getYangTextByteSource().asCharSource(StandardCharsets.UTF_8);
    }
}
