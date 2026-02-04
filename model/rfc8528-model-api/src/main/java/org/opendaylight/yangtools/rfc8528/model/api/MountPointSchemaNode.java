/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'mount-point' extension, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8528">RFC8528</a>, being attached to a SchemaNode.
 */
@Beta
public interface MountPointSchemaNode extends UnknownSchemaNode {
    @Override
    MountPointEffectiveStatement asEffectiveStatement();
}
