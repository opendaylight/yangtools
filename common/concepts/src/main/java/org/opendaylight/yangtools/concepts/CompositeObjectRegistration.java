/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class CompositeObjectRegistration<T> extends AbstractObjectRegistration<T> {

    private final Set<Registration> registrations;

    public CompositeObjectRegistration(final T instance, final Collection<? extends Registration> registrations) {
        super(instance);
        if (registrations == null) {
            throw new IllegalArgumentException();
        }
        this.registrations = Collections.unmodifiableSet(new HashSet<>(registrations));
    }

    @Override
    protected void removeRegistration() {
        for (Registration registration : registrations) {
            try {
                registration.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> CompositeObjectRegistrationBuilder<T> builderFor(final T instance) {
        return new CompositeObjectRegistrationBuilder<>(instance);
    }

    public static final class CompositeObjectRegistrationBuilder<T> implements Builder<CompositeObjectRegistration<T>> {

        private final T instance;
        private final Set<Registration> registrations;

        public CompositeObjectRegistrationBuilder(final T instance) {
            this.instance = instance;
            registrations = new HashSet<>();
        }

        public CompositeObjectRegistrationBuilder<T> add(final ObjectRegistration<? super T> registration) {
            if (!registration.getInstance().equals(instance)) {
                throw new IllegalArgumentException("Instance must be same.");
            }
            registrations.add(registration);
            return this;
        }

        public CompositeObjectRegistrationBuilder<T> remove(final ObjectRegistration<? super T> registration) {
            if (!registration.getInstance().equals(instance)) {
                throw new IllegalArgumentException("Instance must be same.");
            }
            registrations.remove(registration);
            return this;
        }

        @Override
        public CompositeObjectRegistration<T> build() {
            return new CompositeObjectRegistration<>(instance, registrations);
        }

        /*
         * @deprecated Use #build() instead.
         */
        @Deprecated
        public CompositeObjectRegistration<T> toInstance() {
            return build();
        }
    }
}
