/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

/**
 * A {@link BindingYangTextSource} backed by a {@link RootMeta}.
 *
 * @since 16.1.0
 */
@NonNullByDefault
final class MetaYangTextSource extends BindingYangTextSource {
    private final RootMeta<?> rootMeta;

    MetaYangTextSource(final RootMeta<?> rootMeta) {
        this.rootMeta = requireNonNull(rootMeta);
    }

    @Override
    YangModuleInfo moduleInfo() {
        return rootMeta.moduleInfo();
    }

    @Override
    public String symbolicName() {
        return "[" + rootMeta + "]";
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("meta", rootMeta);
    }
}
