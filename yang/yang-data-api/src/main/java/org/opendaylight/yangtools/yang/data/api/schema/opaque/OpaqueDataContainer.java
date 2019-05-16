/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.opaque;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A container-like opaque data node. It can contain other OpaqueDataNodes as its children.
 *
 * @author Robert Varga
 */
@Beta
public interface OpaqueDataContainer extends OpaqueDataNode {

    @NonNull List<? extends OpaqueDataNode> getChildren();
}
