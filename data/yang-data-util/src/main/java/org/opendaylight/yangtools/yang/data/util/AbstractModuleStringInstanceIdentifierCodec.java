/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Extension of {@link AbstractStringInstanceIdentifierCodec}, which instantiates {@link QName}s by first resolving the
 * namespace and then looking the target namespace in the list of currently-subscribed modules.
 */
@Beta
public abstract class AbstractModuleStringInstanceIdentifierCodec extends AbstractStringInstanceIdentifierCodec {
    /**
     * Resolve a string prefix into the corresponding module.
     *
     * @param prefix Prefix
     * @return QNameModule mapped to prefix, or {@code null} if the module cannot be resolved
     */
    protected abstract @Nullable QNameModule moduleForPrefix(@NonNull String prefix);

    @Override
    protected final QName createQName(final String prefix, final String localName) {
        final var module = moduleForPrefix(prefix);
        checkArgument(module != null, "Failed to lookup prefix %s", prefix);
        return QName.create(module, localName);
    }
}
