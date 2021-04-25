/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

final class RefEnumSpecification extends AbstractRefStatement<QName, EnumSpecification> implements EnumSpecification {
    RefEnumSpecification(final EnumSpecification delegate, final DeclarationReference ref) {
        super(delegate, ref);
    }
}
