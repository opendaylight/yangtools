/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * A node representing an instantiation of a
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-10">RFC8040 YANG date template</a>.
 */
@NonNullByDefault
public interface YangDataNode extends DataContainerNode, Identifiable<YangDataNode.Identifier> {
    record Identifier(QNameModule module, QNameModule name) {
        public Identifier {
            requireNonNull(module);
            requireNonNull(name);
        }
    }

    DataContainerChild child();

    @Override
    default Set<DataContainerChild> body() {
        // One less field than Set.of()
        // TODO: re-evaluate when Set.of() is a 'deduped struct' semantics, i.e. cool Valhalla + GC
        return ImmutableSet.of(child());
    }
}
