/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A tree node which has references to its child leaves. These are typically internal non-data leaves, such as
 * containers, lists, etc.
 *
 * @param <C> Final node type
 */
public interface StoreTreeNode<C extends StoreTreeNode<C>> {
    /**
     * Returns a direct child of the node.
     *
     * @param arg Identifier of child
     * @return A node if the child is existing, {@code null} otherwise.
     * @throws NullPointerException when {@code child} is null
     */
    @Nullable C childByArg(@NonNull PathArgument arg);

    /**
     * Returns a direct child of the node.
     *
     * @param arg Identifier of child
     * @return A child node
     * @throws NullPointerException when {@code child} is null
     * @throws VerifyException if the child does not exist
     */
    default @NonNull C getChildByArg(final @NonNull PathArgument arg) {
        return verifyNotNull(childByArg(arg), "Child %s does not exist", arg);
    }

    /**
     * Returns a direct child of the node.
     *
     * @param arg Identifier of child
     * @return Optional with node if the child exists, {@link Optional#empty()} otherwise.
     * @throws NullPointerException when {@code child} is null
     */
    default @NonNull Optional<C> findChildByArg(final @NonNull PathArgument arg) {
        return Optional.ofNullable(childByArg(arg));
    }
}
