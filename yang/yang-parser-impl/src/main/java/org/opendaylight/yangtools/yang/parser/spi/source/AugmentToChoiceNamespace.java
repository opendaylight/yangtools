/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * namespace key class for storing augment nodes which are going to be augmented as
 * shortHand case nodes into choice node
 */
public interface AugmentToChoiceNamespace extends IdentifierNamespace<StmtContext<?, ?, ?>, Boolean> {

}