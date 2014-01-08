/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.event;

import java.util.Date;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * An #{@link EventStreamContext} which supports dispatching events to one or more listeners.
 */
public interface ListenableEventStreamContext extends EventStreamContext {
	/**
	 * Register a new listener to which events will be delivered until the returned registration is not closed.
	 * 
	 * @param listener New listener
	 * @return Listener registration handle
	 */
	<T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(T listener);

	/**
	 * Subscribe to the underlying event stream. Equivalent to {@link #startListeningWithReplay(Optional, Optional)} with
	 * both arguments being Optional.absent().
	 * 
	 * @return Future representing the asynchronous request for subscription.
	 * @throws IllegalStateException if the event stream is currently being listened on
	 */
	ListenableFuture<RpcResult<Void>> startListening();

	/**
	 * Subscribe to the underlying event stream and attempt to replay events between startTime.
	 * 
	 * @param startTime Start time. If present, required to be in the past. Omitting this parameter results
	 *                  in the server going as far as possible.
	 * @param endTime End time. If present, required to be later than start time.
	 * @return Future representing the asynchronous request for subscription.
	 * @throws IllegalStateException if the event stream is currently being listened on
	 */
	ListenableFuture<RpcResult<Void>> startListeningWithReplay(Optional<Date> startTime, Optional<Date> endTime);

	/**
	 * Stop listening to the underlying event stream.
	 * @throws IllegalStateException if the event stream is currently not being listened on
	 */
	void stopListening();
}
