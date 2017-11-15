/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Identity namespace. All identity names defined in a module and its submodules share the same identity identifier
 * namespace.
 */
// FIXME: describe scoping and value
public interface DerivedIdentitiesNamespace extends IdentifierNamespace<QName, List<StmtContext<?, ?, ?>>> {
    NamespaceBehaviour<QName, List<StmtContext<?, ?, ?>>, @NonNull DerivedIdentitiesNamespace> BEHAVIOUR =
            NamespaceBehaviour.global(DerivedIdentitiesNamespace.class);

}
