/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;

// FIXME: 8.0.0: re-examine usefulness of these methods
@Beta
public final class NormalizedNodeSchemaUtils {
    private NormalizedNodeSchemaUtils() {
        // Hidden on purpose
    }

    public static Optional<CaseSchemaNode> detectCase(final ChoiceSchemaNode schema, final DataContainerChild child) {
        final QName childId = child.name().getNodeType();
        for (var choiceCaseNode : schema.getCases()) {
            if (choiceCaseNode.dataChildByName(childId) != null) {
                return Optional.of(choiceCaseNode);
            }
        }
        return Optional.empty();
    }
}
