/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Interface extended by all interfaces generated for a YANG {@code action} instantiated in keyed lists.
 */
public interface KeyedListAction<K extends Key<T>, T extends EntryObject<T, K>,
        I extends RpcInput, O extends RpcOutput> extends Action<WithKey<T, K>, I, O> {
    @Override
    @CheckReturnValue
    ListenableFuture<RpcResult<O>> invoke(WithKey<T, K> path, I input);
}
