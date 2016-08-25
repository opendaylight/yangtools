/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.immutable;

import java.util.Collection;
import java.util.Map;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

@Value.Immutable
abstract class AbstractDataNodeContainer implements DataNodeContainer {

    abstract Map<QName, DataSchemaNode> children();

    @Override
    public final Collection<DataSchemaNode> getChildNodes() {
        return children().values();
    }

    @Override
    public final DataSchemaNode getDataChildByName(final QName name) {
        return children().get(name);
    }
}
