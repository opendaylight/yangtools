/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

final class UnmodifiedLeafCandidateNode extends AbstractAvailableLeafCandidateNode {
    UnmodifiedLeafCandidateNode(final NormalizedNode data) {
        super(data);
    }

    @Override
    public ModificationType modificationType() {
        return ModificationType.UNMODIFIED;
    }

    @Override
    public NormalizedNode dataBefore() {
        return data;
    }
}
