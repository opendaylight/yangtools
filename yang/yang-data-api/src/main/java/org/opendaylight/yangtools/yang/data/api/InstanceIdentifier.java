package org.opendaylight.yangtools.yang.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Path;
import org.opendaylight.yangtools.yang.common.QName;

public class InstanceIdentifier implements Path<InstanceIdentifier>, Immutable {

    private final List<PathArgument> path;

    public List<PathArgument> getPath() {
        return path;
    }

    public InstanceIdentifier(final List<? extends PathArgument> path) {
        this.path = Collections.unmodifiableList(new ArrayList<>(path));
    }

    private InstanceIdentifier(NodeIdentifier nodeIdentifier) {
        this.path = Collections.<PathArgument> singletonList(nodeIdentifier);
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

    public InstanceIdentifier of(QName name) {
        return new InstanceIdentifier(new NodeIdentifier(name));
    }

    public InstanceIdentifierBuilder builder() {
        return new BuilderImpl();
    }

    public interface PathArgument {
        QName getNodeType();

    }

    public interface InstanceIdentifierBuilder {
        InstanceIdentifierBuilder node(QName nodeType);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, Map<QName, Object> keyValues);

        InstanceIdentifierBuilder nodeWithKey(QName nodeType, QName key, Object value);

        InstanceIdentifier getIdentifier();
    }

    public static final class NodeIdentifier implements PathArgument {

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
    }

    public static final class NodeIdentifierWithPredicates implements PathArgument {
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
    }

    private static class BuilderImpl implements InstanceIdentifierBuilder {

        private List<PathArgument> path;

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
        public InstanceIdentifier getIdentifier() {
            return new InstanceIdentifier(path);
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
}
