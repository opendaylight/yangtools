/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 *
 * Statement that defines new data nodes.
 *
 * One of container, leaf, leaf-list, list, choice, case,
 * augment, uses, and anyxml.
 *
 *
 * Defined in: https://tools.ietf.org/html/rfc6020#section-3
 *
 * @param <S> Concrete statement type - final interface / class which implements this interface.
 */
@Rfc6020AbnfRule("data-def-stmt")
public interface DataDefinitionStatement extends DeclaredStatement<QName>, DocumentationGroup.WithStatus, ConditionalDataDefinition {

    @Nonnull QName getName();

}
