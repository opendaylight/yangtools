/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeComplexUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyUsesAugmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUses;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.complex.from.grouping.ListViaUsesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.TopBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.top.level.list.NestedListKey;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

public final class ListsBindingUtils {
    private static final DataObjectIdentifier<Top> TOP_PATH = DataObjectIdentifier.builder(Top.class).build();

    public static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    public static final TopLevelListKey TOP_BAR_KEY = new TopLevelListKey("bar");
    public static final ListViaUsesKey USES_ONE_KEY = new ListViaUsesKey("one");
    public static final ListViaUsesKey USES_TWO_KEY = new ListViaUsesKey("two");

    private ListsBindingUtils() {
        // Hidden on purpose
    }

    public static @NonNull WithKey<TopLevelList, TopLevelListKey> path(final TopLevelListKey key) {
        return TOP_PATH.toBuilder().child(TopLevelList.class, key).build();
    }

    public static @NonNull WithKey<NestedList, NestedListKey> path(final TopLevelListKey top,
            final NestedListKey nested) {
        return path(top).toBuilder().child(NestedList.class, nested).build();
    }

    public static @NonNull WithKey<ListViaUses, ListViaUsesKey> path(final TopLevelListKey top,
            final ListViaUsesKey uses) {
        return path(top).toBuilder()
            .augmentation(TreeComplexUsesAugment.class)
            .child(ListViaUses.class, uses)
            .build();
    }

    public static <T extends Augmentation<TopLevelList>> DataObjectIdentifier<T> path(final TopLevelListKey key,
            final Class<T> augmentation) {
        return path(key).toBuilder().augmentation(augmentation).build();
    }

    public static Top top() {
        return new TopBuilder().build();
    }

    public static Top top(final TopLevelList... listItems) {
        return new TopBuilder().setTopLevelList(Maps.uniqueIndex(Arrays.asList(listItems), TopLevelList::key)).build();
    }

    public static TopLevelList topLevelList(final TopLevelListKey key) {
        return new TopLevelListBuilder().withKey(key).build();
    }

    public static TopLevelList topLevelList(final TopLevelListKey key, final Augmentation<TopLevelList> augment) {
        return new TopLevelListBuilder().withKey(key).addAugmentation(augment).build();
    }

    public static TreeComplexUsesAugment complexUsesAugment(final ListViaUsesKey... keys) {
        final var listViaUses = ImmutableMap.<ListViaUsesKey, ListViaUses>builder();
        for (var key : keys) {
            listViaUses.put(key, new ListViaUsesBuilder().withKey(key).build());
        }
        return new TreeComplexUsesAugmentBuilder().setListViaUses(listViaUses.build()).build();
    }

    public static TreeLeafOnlyUsesAugment leafOnlyUsesAugment(final String leafFromGroupingValue) {
        return new TreeLeafOnlyUsesAugmentBuilder().setLeafFromGrouping(leafFromGroupingValue).build();
    }
}
