/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class YangInstanceIdentifierBuilder implements InstanceIdentifierBuilder {
    private final HashCodeBuilder<PathArgument> hash;
    private final List<PathArgument> path;

    public YangInstanceIdentifierBuilder() {
        this.hash = new HashCodeBuilder<>();
        this.path = new ArrayList<>();
    }

    public YangInstanceIdentifierBuilder(final Iterable<PathArgument> prefix, final int hash) {
        this.path = Lists.newArrayList(prefix);
        this.hash = new HashCodeBuilder<>(hash);
    }

    @Override
    public InstanceIdentifierBuilder node(final QName nodeType) {
        final PathArgument arg = new NodeIdentifier(nodeType);
        path.add(arg);
        hash.addArgument(arg);
        return this;
    }

    @Override
    public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final QName key, final Object value) {
        final PathArgument arg = new NodeIdentifierWithPredicates(nodeType, key, value);
        path.add(arg);
        hash.addArgument(arg);
        return this;
    }

    @Override
    public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final Map<QName, Object> keyValues) {
        final PathArgument arg = new NodeIdentifierWithPredicates(nodeType, keyValues);
        path.add(arg);
        hash.addArgument(arg);
        return this;
    }

    @Override
    @Deprecated
    public YangInstanceIdentifier toInstance() {
        return build();
    }

    @Override
    public YangInstanceIdentifier build() {
        return FixedYangInstanceIdentifier.create(path, hash.build());
    }
}