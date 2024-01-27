/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public interface QNameModuleAware {
    /**
     * Returns a {@link QNameModule}, which contains the namespace and the revision of the module.
     *
     * @return QNameModule identifier.
     */
    @NonNull QNameModule getQNameModule();

    default @NonNull XMLNamespace getNamespace() {
        return getQNameModule().namespace();
    }

    default @NonNull Optional<Revision> getRevision() {
        return getQNameModule().findRevision();
    }
}
