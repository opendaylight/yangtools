/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

@Beta
public final class BindingSchemaMapping {
    private BindingSchemaMapping() {

    }

    public static String getGetterMethodName(final DataSchemaNode node) {
        return Naming.getGetterMethodName(node.getQName());
    }
}
