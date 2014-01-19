/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.common.QName;

import com.google.common.collect.ImmutableList;

public class InstanceIdentifier implements Path<InstanceIdentifier>, Immutable, Serializable {

    private static final long serialVersionUID = 8467409862384206193L;
    private final List<PathArgument> path;

    private transient String to_string_cache = null;

    public List<PathArgument> getPath() {
        return path;
    }

    public InstanceIdentifier(final List<? extends PathArgument> path) {
        this.path =ImmutableList.copyOf(path);
    }

    private InstanceIdentifier(NodeIdentifier nodeIdentifier) {
        this.path = ImmutableList.<PathArgument>of(nodeIdentifier);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InstanceIdentifier other = (InstanceIdentifier) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

    // Static factories & helpers

    public static InstanceIdentifier of(QName name) {
        return new InstanceIdentifier(new NodeIdentifier(name));
    }

    static public InstanceIdentifierBuilder builder() {
        return new BuilderImpl();
    }

    static public InstanceIdentifierBuilder builder(InstanceIdentifier origin) {
        return new BuilderImpl(origin.getPath());
    }

    public interface PathArgument extends Immutable, Serializable {
        QName getNodeType();

    }

    public interface InstanceIdentifierBuilder extends Builder<InstanceIdentifier> {
        InstanceIdentifierBuilder node(QName nodeType);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value);

        @Deprecated
        InstanceIdentifier getIdentifier();
    }

    public static final class NodeIdentifier implements PathArgument {

        /**
         * 
         */
        private static final long serialVersionUID = -2255888212390871347L;

        private final QName nodeType;

        public NodeIdentifier(QName node) {
            this.nodeType = node;
        }

        public QName getNodeType() {
            return nodeType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NodeIdentifier other = (NodeIdentifier) obj;
            if (nodeType == null) {
                if (other.nodeType != null)
                    return false;
            } else if (!nodeType.equals(other.nodeType))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return nodeType.toString();
        }
    }

    public static final class NodeIdentifierWithPredicates implements PathArgument {

        /**
         * 
         */
        private static final long serialVersionUID = -4787195606494761540L;

        private final QName nodeType;
        private final Map<QName, Object> keyValues;

        public NodeIdentifierWithPredicates(QName node, Map<QName, Object> keyValues) {
            this.nodeType = node;
            this.keyValues = Collections.unmodifiableMap(new HashMap<QName, Object>(keyValues));
        }

        public NodeIdentifierWithPredicates(QName node, QName key, Object value) {
            this.nodeType = node;
            this.keyValues = Collections.singletonMap(key, value);
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        public Map<QName, Object> getKeyValues() {
            return keyValues;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((keyValues == null) ? 0 : keyValues.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NodeIdentifierWithPredicates other = (NodeIdentifierWithPredicates) obj;
            if (keyValues == null) {
                if (other.keyValues != null)
                    return false;
            } else if (!keyValues.equals(other.keyValues))
                return false;
            if (nodeType == null) {
                if (other.nodeType != null)
                    return false;
            } else if (!nodeType.equals(other.nodeType))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return nodeType + "[" + keyValues + "]";
        }
    }

    private static class BuilderImpl implements InstanceIdentifierBuilder {

        private final ImmutableList.Builder<PathArgument> path; 

        public BuilderImpl() {
            path = ImmutableList.<PathArgument>builder();
        }

        public BuilderImpl(List<? extends PathArgument> prefix) {
            path = ImmutableList.<PathArgument>builder();
            path.addAll(prefix);
        }

        @Override
        public InstanceIdentifierBuilder node(QName nodeType) {
            path.add(new NodeIdentifier(nodeType));
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value) {
            path.add(new NodeIdentifierWithPredicates(nodeType, key, value));
            return this;
        }

        @Override
        public InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues) {
            path.add(new NodeIdentifierWithPredicates(nodeType, keyValues));
            return this;
        }

        @Override
        public InstanceIdentifier toInstance() {
            return new InstanceIdentifier(path.build());
        }

        @Override
        public InstanceIdentifier getIdentifier() {
            return toInstance();
        }
    }

    @Override
    public boolean contains(final InstanceIdentifier other) {
        if (other == null) {
            throw new IllegalArgumentException("other should not be null");
        }
        final int localSize = this.path.size();
        final List<PathArgument> otherPath = other.getPath();
        if (localSize > other.path.size()) {
            return false;
        }
        for (int i = 0; i < localSize; i++) {
            if (!path.get(i).equals(otherPath.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (to_string_cache != null) {
            return to_string_cache;
        }
        StringBuilder builder = new StringBuilder();
        for (PathArgument argument : path) {
            builder.append("/");
            builder.append(argument.toString());
        }
        to_string_cache = builder.toString();
        return to_string_cache;
    }

    public static InstanceIdentifierBuilder builder(QName node) {
        return builder().node(node);
    }
}
