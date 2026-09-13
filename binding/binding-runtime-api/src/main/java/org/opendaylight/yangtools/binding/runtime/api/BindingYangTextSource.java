/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;

/**
 * A {@link YangTextSource} backed by Binding Specification packaging constructs.
 *
 * @since 16.1.0
 */
@NonNullByDefault
public abstract sealed class BindingYangTextSource extends YangTextSource
        permits InfoYangTextSource, MetaYangTextSource {
    /**
     * {@return a {@code YangTextSource} corresponding to the module described by {@code rootMeta}}
     *
     * @param rootMeta the {@link RootMeta}
     */
    public static BindingYangTextSource of(final RootMeta<?> rootMeta) {
        return new MetaYangTextSource(rootMeta);
    }

    /**
     * {@return a {@code YangTextSource} corresponding to the module described by {@code moduleInfo}}
     *
     * @param moduleInfo the {@link YangModuleInfo}
     */
    public static BindingYangTextSource of(final YangModuleInfo moduleInfo) {
        return new InfoYangTextSource(moduleInfo);
    }

    @Override
    public final Reader openStream() throws IOException {
        return new InputStreamReader(moduleInfo().openYangTextStream(), StandardCharsets.UTF_8);
    }

    @Override
    public final SourceIdentifier sourceId() {
        return SourceIdentifier.ofQName(moduleInfo().name());
    }

    @Override
    public abstract @NonNull String symbolicName();

    @Override
    protected abstract ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);

    /**
     * {@return the backing {@link YangModuleInfo}}
     */
    abstract YangModuleInfo moduleInfo();
}
