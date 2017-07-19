/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public interface XmlCodecProvider {

    TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType);

}
