/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;

/**
 * An {@code anydata} node with an {@link NormalizedNodeContainer} payload.
 */
@SuppressWarnings("rawtypes")
@Beta
public interface NormalizedAnydataNode extends AnydataNode<NormalizedNodeContainer> {
    @Override
    default Class<NormalizedNodeContainer> getValueObjectModel() {
        return NormalizedNodeContainer.class;
    }
}
