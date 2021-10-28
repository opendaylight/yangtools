/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class YangInstanceIdentifierBuilder implements InstanceIdentifierBuilder {
    private final List<PathArgument> path;

    YangInstanceIdentifierBuilder() {
        path = new ArrayList<>();
    }

    YangInstanceIdentifierBuilder(final List<PathArgument> prefix) {
        path = new ArrayList<>(prefix);
    }

    private @NonNull InstanceIdentifierBuilder addArgument(final PathArgument arg) {
        path.add(arg);
        return this;
    }

    @Override
    public InstanceIdentifierBuilder node(final PathArgument arg) {
        return addArgument(requireNonNull(arg));
    }

    @Override
    public InstanceIdentifierBuilder node(final QName nodeType) {
        return addArgument(new NodeIdentifier(nodeType));
    }

    @Override
    public InstanceIdentifierBuilder append(final Collection<? extends PathArgument> args) {
        path.addAll(args);
        return this;
    }

    @Override
    public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final QName key, final Object value) {
        return addArgument(NodeIdentifierWithPredicates.of(nodeType, key, value));
    }

    @Override
    public InstanceIdentifierBuilder nodeWithKey(final QName nodeType, final Map<QName, Object> keyValues) {
        return addArgument(NodeIdentifierWithPredicates.of(nodeType, keyValues));
    }

    @Override
    public YangInstanceIdentifier build() {
        return FixedYangInstanceIdentifier.of(path);
    }
}
