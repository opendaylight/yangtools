/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uniquely identifies instance of data tree.
 * 
 * 
 */
public class InstanceIdentifier {

    private final List<PathArgument> path;
    private final Class<? extends DataObject> targetType;

    public InstanceIdentifier(Class<? extends DataObject> type) {
        path = Collections.emptyList();
        this.targetType = type;
    }

    public InstanceIdentifier(List<PathArgument> path, Class<? extends DataObject> type) {
        this.path = Collections.<PathArgument> unmodifiableList(new ArrayList<>(path));
        this.targetType = type;
    }

    /**
     * 
     * @return path
     */
    public List<PathArgument> getPath() {
        return this.path;
    }

    public Class<?> getTargetType() {
        return this.targetType;
    }

    @Override
    public String toString() {
        return "InstanceIdentifier [path=" + path + "]";
    }

    /**
     * Path argument of instance identifier.
     * 
     * Interface which implementations are used as path components of the
     * instance path.
     * 
     * @author ttkacik
     * 
     */
    public interface PathArgument {

    }

    public static class IdentifiableItem<I extends Identifiable<T>, T extends Identifier<I>> implements PathArgument {

        private final T key;
        private final Class<? extends I> type;

        public IdentifiableItem(Class<I> type, T key) {
            this.type = type;
            this.key = key;
        }

        T getKey() {
            return this.key;
        }

        Class<? extends I> getType() {
            return this.type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.hashCode() != hashCode()) {
                return false;
            }
            if (!(obj instanceof IdentifiableItem<?, ?>)) {
                return false;
            }
            IdentifiableItem<?, ?> foreign = (IdentifiableItem<?, ?>) obj;
            return key.equals(foreign.getKey());
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return "IdentifiableItem [key=" + key + "]";
        }
    }
}
