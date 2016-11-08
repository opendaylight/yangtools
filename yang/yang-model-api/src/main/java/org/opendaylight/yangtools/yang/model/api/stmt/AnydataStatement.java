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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The "anydata" statement defines an interior node in the schema tree.
 * It takes one argument, which is an identifier, followed by a block of
 * substatements that holds detailed anydata information.
 *
 * The "anydata" statement is used to represent an unknown set of nodes
 * that can be modeled with YANG, except anyxml, but for which the data
 * model is not known at module design time.  It is possible, though not
 * required, for the data model for anydata content to become known
 * through protocol signaling or other means that are outside the scope
 * of this document.
 */
@Beta
public interface AnydataStatement extends DataDefinitionStatement {

    @Nonnull
    Collection<? extends MustStatement> getMusts();

    @Nullable
    ConfigStatement getConfig();

    @Nullable MandatoryStatement getMandatory();
}
