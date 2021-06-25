/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal448Test {
    @Test
    public void groupingSortIncludesActions() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource("/mdsal448.yang");
        final Collection<? extends GroupingDefinition> groupings = context.findModule("mdsal448").get().getGroupings();
        assertEquals(2, groupings.size());

        final List<GroupingDefinition> ordered = sortGroupings(Iterables.get(groupings, 0),
            Iterables.get(groupings, 1));
        assertEquals(2, ordered.size());
        // "the-grouping" needs to be first
        assertEquals("the-grouping", ordered.get(0).getQName().getLocalName());
        assertEquals("action-grouping", ordered.get(1).getQName().getLocalName());

        // Sort needs to be stable
        final List<GroupingDefinition> reverse = sortGroupings(Iterables.get(groupings, 1),
            Iterables.get(groupings, 0));
        assertEquals(ordered, reverse);

        final List<GeneratedType> types = DefaultBindingGenerator.generateFor(context);
        assertNotNull(types);
        assertEquals(9, types.size());
    }

    private static List<GroupingDefinition> sortGroupings(final GroupingDefinition... groupings) {
        return GroupingDefinitionDependencySort.sort(Arrays.asList(groupings));
    }
}
