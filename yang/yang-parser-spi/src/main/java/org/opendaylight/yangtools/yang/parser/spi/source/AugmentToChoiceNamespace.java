/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * namespace key class for storing augment nodes which are going to be augmented as
 * shortHand case nodes into choice node.
 *
 * @deprecated This namespace is no longer used anywhere and not supported by the default reactor. It is scheduled
 *             for removal.
 */
@Deprecated
public interface AugmentToChoiceNamespace extends IdentifierNamespace<StmtContext<?, ?, ?>, Boolean> {
    NamespaceBehaviour<StmtContext<?, ?, ?>, Boolean, @NonNull AugmentToChoiceNamespace> BEHAVIOUR =
            NamespaceBehaviour.treeScoped(AugmentToChoiceNamespace.class);

}
