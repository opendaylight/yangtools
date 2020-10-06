/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.net.URI;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public interface QNameModuleAware {
    /**
     * Returns a {@link QNameModule}, which contains the namespace and the revision of the module.
     *
     * @return QNameModule identifier.
     */
    @NonNull QNameModule getQNameModule();

    @Deprecated
    default @NonNull URI getNamespace() {
        return getQNameModule().getNamespace();
    }

    default @NonNull Optional<Revision> getRevision() {
        return getQNameModule().getRevision();
    }
}
