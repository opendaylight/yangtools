/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.DerivedType;

/**
 * Derived types namespace
 *
 * similar to {@link TypeNamespace} but contains some kind of helper type
 * definitions from that can type information gained later
 *
 */

public interface QNameToDerivedType extends IdentifierNamespace<QName, DerivedType> {
}
