/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common interface for action and rpc statements.
 *
 * @deprecated Use {@link OperationDeclaredStatement} instead.
 */
@Deprecated
@Beta
public interface OperationGroup extends DocumentationGroup.WithStatus, ConditionalFeature {

    @NonNull QName getName();

    @NonNull Collection<? extends TypedefStatement> getTypedefs();

    @NonNull Collection<? extends GroupingStatement> getGroupings();

    @Nullable InputStatement getInput();

    @Nullable OutputStatement getOutput();
}
