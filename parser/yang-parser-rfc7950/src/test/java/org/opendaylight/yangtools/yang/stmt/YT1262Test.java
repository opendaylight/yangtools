/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefAwareEffectiveStatement;

public class YT1262Test extends AbstractYangTest {
    @Test
    public void testTypedefNamespaces() {
        final var modelContext = assertEffectiveModelDir("/bugs/YT1262");
        final var module = modelContext.getModuleStatement(QNameModule.create(XMLNamespace.of("foo")));
        assertTypedef(module, "fdef");
        assertTypedef(module, "sdef");
        assertTypedef(module.findFirstEffectiveSubstatement(GroupingEffectiveStatement.class).orElseThrow(), "gdef");
        assertTypedef(module.findFirstEffectiveSubstatement(ListEffectiveStatement.class).orElseThrow(), "ldef");
        assertTypedef(module.findFirstEffectiveSubstatement(NotificationEffectiveStatement.class).orElseThrow(),
            "ndef");
        assertTypedef(module.findFirstEffectiveSubstatement(RpcEffectiveStatement.class).orElseThrow(), "rdef");

        final var container = module.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertTypedef(container, "cdef");

        final var action = container.findFirstEffectiveSubstatement(ActionEffectiveStatement.class).orElseThrow();
        assertTypedef(action, "adef");
        assertTypedef(action.findFirstEffectiveSubstatement(InputEffectiveStatement.class).orElseThrow(), "idef");
        assertTypedef(action.findFirstEffectiveSubstatement(OutputEffectiveStatement.class).orElseThrow(), "odef");
    }

    private static void assertTypedef(final EffectiveStatement<?, ?> parent, final String typedefName) {
        assertThat(parent, instanceOf(TypedefAwareEffectiveStatement.class));
        assertTrue(((TypedefAwareEffectiveStatement<?, ?>) parent).findTypedef(QName.create("foo", typedefName))
            .isPresent());
    }
}
