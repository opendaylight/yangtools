/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.GlobalIdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.GlobalStatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Feature namespace. All feature names defined in a module and its submodules share the same feature identifier
 * namespace. Each feature is identified by a QName formed from the defining module's QNameModule and the feature name.
 *
 * <p>
 * Since we required unique QName to be assigned to each module, their feature names are globally unique, hence this
 * is a {@link GlobalIdentifierNamespace}.
 */
public interface FeatureNamespace extends GlobalStatementNamespace<QName, FeatureStatement, FeatureEffectiveStatement> {
    NamespaceBehaviour<QName, StmtContext<?, FeatureStatement, FeatureEffectiveStatement>,
            @NonNull FeatureNamespace> BEHAVIOUR = NamespaceBehaviour.globalOf(FeatureNamespace.class);

}
