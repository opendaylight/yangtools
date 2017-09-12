/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

/**
 * Feature namespace. All feature names defined in a module and its submodules share the
 * same feature identifier namespace. Each feature is identified by a QName formed from
 * the defining module's QNameModule and the feature name.
 */
public interface FeatureNamespace
    extends StatementNamespace<QName, FeatureStatement, EffectiveStatement<QName, FeatureStatement>> {

}
