/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.api;

import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.leafrefcontext.builder.QNameWithPredicateBuilder;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.common.QNameModule;

public interface QNameWithPredicate {

    public static final QNameWithPredicate UP_PARENT = new QNameWithPredicateBuilder(
            null, "..").build();

    public static final QNameWithPredicate ROOT = new QNameWithPredicateBuilder(
            null, "").build();

    public LinkedList<QNamePredicate> getQNamePredicates();

    public QNameModule getModuleQname();

    public String getLocalName();

    public QName getQName();

}