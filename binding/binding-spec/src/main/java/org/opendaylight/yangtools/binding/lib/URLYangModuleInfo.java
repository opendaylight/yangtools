/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link YangModuleInfo} backed by {@link URL}-based streams.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public sealed interface URLYangModuleInfo extends YangModuleInfo permits URLYangModuleInfoImpl {
    /**
     * {@return a new {@link URLYangModuleInfo} with specified name, streams supplied by specified {@link URL} and no
     * imported modules}
     * @param name the name
     * @param url the URL
     * @since 16.0.0
     */
    static URLYangModuleInfo of(final QName name, final URL url) {
        return new URLYangModuleInfoImpl(name, url, List.of());
    }

    /**
     * {@return a new {@link YangModuleInfo} with specified name, streams supplied by specified {@link URL} and
     * specified imported modules}
     *
     * @param name the name
     * @param url the URL
     * @param importedModules imported modules
     * @since 16.0.0
     */
    static URLYangModuleInfo of(final QName name, final URL url, final YangModuleInfo... importedModules) {
        return new URLYangModuleInfoImpl(name, url, List.of(importedModules));
    }

    /**
     * {@return the URL serving UTF-8-encoded YANG text}
     */
    URL url();

    @Override
    default InputStream openYangTextStream() throws IOException {
        return url().openStream();
    }
}
