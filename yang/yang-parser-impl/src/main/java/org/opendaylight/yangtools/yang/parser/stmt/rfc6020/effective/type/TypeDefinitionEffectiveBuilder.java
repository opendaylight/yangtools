/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *  Effective statements of TypeDef, ExtendedType, Decimal64, Enumeration, Leafref, Union, IndetityRef, Bits
 *  should implement this interface and method buildType() should create particular object from
 *  yang.model.util (e.g. Decimal64)
 */
public interface TypeDefinitionEffectiveBuilder {

    TypeDefinition<?> buildType();

}
