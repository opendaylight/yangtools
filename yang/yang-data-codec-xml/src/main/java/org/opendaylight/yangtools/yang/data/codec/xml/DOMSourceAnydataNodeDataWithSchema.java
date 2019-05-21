/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import javax.xml.transform.dom.DOMSource;
import org.opendaylight.yangtools.yang.data.util.AbstractAnydataNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

final class DOMSourceAnydataNodeDataWithSchema extends AbstractAnydataNodeDataWithSchema<DOMSource> {
    DOMSourceAnydataNodeDataWithSchema(final AnyDataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    protected Class<DOMSource> objectModelClass() {
        return DOMSource.class;
    }
}
