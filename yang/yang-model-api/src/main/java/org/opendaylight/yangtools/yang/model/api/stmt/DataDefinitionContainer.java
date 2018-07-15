/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nonnull;

@Deprecated
public interface DataDefinitionContainer {

    @Nonnull Collection<? extends DataDefinitionStatement> getDataDefinitions();

    interface WithReusableDefinitions extends DataDefinitionContainer {

        @Nonnull Collection<? extends TypedefStatement> getTypedefs();

        @Nonnull Collection<? extends GroupingStatement> getGroupings();
    }
}
