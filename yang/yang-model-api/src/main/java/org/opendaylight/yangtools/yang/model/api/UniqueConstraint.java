/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;

/**
 * Interface describing YANG 'unique' constraint.
 *
 * <p>
 * The 'unique' constraint specifies that the combined values of all the leaf instances specified in the argument
 * string, including leafs with default values, MUST be unique within all list entry instances in which all referenced
 * leafs exist (for more information see RFC-6020 section 7.8.3.).
 */
@Beta
public interface UniqueConstraint {
    @NonNull Set<Relative> getTag();
}
