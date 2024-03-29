/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public interface QNameWithPredicate {

    @NonNull QNameWithPredicate UP_PARENT = new QNameWithPredicateBuilder(null, "..").build();

    @NonNull QNameWithPredicate ROOT = new QNameWithPredicateBuilder(null, "").build();

    List<QNamePredicate> getQNamePredicates();

    QNameModule getModuleQname();

    String getLocalName();

    QName getQName();

}
