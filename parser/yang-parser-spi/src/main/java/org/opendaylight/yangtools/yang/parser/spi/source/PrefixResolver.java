/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

/**
 * Interface for resolving XML prefixes to their bound {@link QNameModule}s. This resolution entails determining
 * the correct {@link Revision} bound at the use site.
 */
public interface PrefixResolver {
    /**
     * Returns QNameModule (namespace + revision) associated with supplied prefix.
     *
     * @param prefix Prefix
     * @return QNameModule associated with supplied prefix, or null if prefix is not defined.
     */
    @Nullable QNameModule resolvePrefix(@NonNull String prefix);
}
