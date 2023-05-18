/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.common.YangDataName;

/**
 * The contents of a {@code yang-data} template instance, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-80">RFC8040</a>'s {@code ietf-restconf} module.
 */
public interface NormalizedYangData extends NormalizedData {
    @Override
    default Class<NormalizedYangData> contract() {
        return NormalizedYangData.class;
    }

    @Override
    YangDataName name();
}
