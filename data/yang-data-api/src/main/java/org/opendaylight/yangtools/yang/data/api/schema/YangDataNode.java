/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * A node representing an instantiation of a
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-10">RFC8040 YANG date template</a>.
 */
// FIXME: getIdentifier() does not work here!
@NonNullByDefault
public interface YangDataNode extends DataContainerNode {

    QNameModule module();

    String name();

    DataContainerChild child();

    @Override
    default Set<DataContainerChild> body() {
        return ImmutableSet.of(child());
    }
}
