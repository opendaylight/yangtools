/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * The "YangModeledAnyXml" interface defines an interior node in the schema tree. It takes
 * one argument, which is an identifier represented by QName inherited from
 * {@link SchemaNode}, followed by a block of substatements that holds detailed
 * anyxml information. The substatements are defined in {@link DataSchemaNode}.
 * The "YangModeledAnyXml" in contrast to the "AnyXml" interface can also provide schema
 * of contained XML information. <br>
 * <br>
 * This interface was modeled according to definition in <a
 * href="https://tools.ietf.org/html/rfc6020#section-7.10">[RFC-6020] The anyxml
 * Statement</a>
 */
@Beta
public interface YangModeledAnyXmlSchemaNode extends AnyXmlSchemaNode {

    /**
     * Returns the root schema node of the data in this anyxml node.
     *
     * @return DataSchemaNode - schema of contained XML data
     */
    @NonNull ContainerSchemaNode getSchemaOfAnyXmlData();
}
