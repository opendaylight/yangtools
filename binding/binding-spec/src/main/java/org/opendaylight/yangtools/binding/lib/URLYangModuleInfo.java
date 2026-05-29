/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link YangModuleInfo} backed by {@link URL}-based streams.
 *
 * @since 16.0.0
 */
@NonNullByDefault
public final class URLYangModuleInfo implements YangModuleInfo {
    private static final Logger LOG = LoggerFactory.getLogger(URLYangModuleInfo.class);
    private static final Comparator<YangModuleInfo> MODULE_INFO_COMPARATOR = Comparator.comparing(YangModuleInfo::name);

    private final QName name;
    private final URL url;
    private final List<YangModuleInfo> imports;

    private URLYangModuleInfo(final QName name, final URL url, final List<YangModuleInfo> imports) {
        this.name = requireNonNull(name);
        this.url = requireNonNull(url);
        this.imports = requireNonNull(imports);

        long bytesAvail;
        try {
            bytesAvail = url.openStream().transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOG.debug("{} has {} bytes and {} imported module(s)", url, bytesAvail, imports.size());
    }

    /**
     * {@return a new {@link URLYangModuleInfo} with specified name, streams supplied by specified {@link URL} and no
     * imported modules}
     * @param name the name
     * @param url the URL
     * @since 16.0.0
     */
    public static URLYangModuleInfo of(final QName name, final URL url) {
        return new URLYangModuleInfo(name, url, List.of());
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
    public static URLYangModuleInfo of(final QName name, final URL url, final YangModuleInfo... importedModules) {
        return new URLYangModuleInfo(name, url, List.copyOf(Arrays.stream(importedModules)
            .collect(Collectors.toCollection(() -> new TreeSet<>(MODULE_INFO_COMPARATOR)))));
    }

    @Override
    public QName name() {
        return name;
    }

    @Override
    public List<YangModuleInfo> getImportedModules() {
        return imports;
    }

    @Override
    public InputStream openYangTextStream() throws IOException {
        return url.openStream();
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder("URLYangModuleInfo{name=");
        appendName(sb, name);
        sb.append(", url=").append(url.toExternalForm());
        if (!imports.isEmpty()) {
            sb.append(", imports=[");
            final var it = imports.iterator();
            while (true) {
                final var info = it.next();
                appendName(sb, info.name());
                if (info instanceof URLYangModuleInfo urlInfo) {
                    sb.append('=').append(urlInfo.url.toExternalForm());
                }
                if (!it.hasNext()) {
                    break;
                }
                sb.append(", ");
            }
            sb.append(']');
        }
        return sb.append('}').toString();
    }

    private static void appendName(final StringBuilder sb, final QName name) {
        sb.append(name.getLocalName());
        if (name.getModule().revisionUnion() instanceof Revision revision) {
            sb.append('@').append(revision.toString());
        }
    }
}
