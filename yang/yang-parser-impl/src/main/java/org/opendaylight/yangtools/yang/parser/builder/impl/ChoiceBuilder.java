/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class ChoiceBuilder extends AbstractSchemaNodeBuilder implements DataSchemaNodeBuilder,
        AugmentationTargetBuilder {
    private boolean isBuilt;
    private final ChoiceNodeImpl instance;
    // DataSchemaNode args
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();
    // ChoiceNode args
    private Set<ChoiceCaseNode> cases = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<ChoiceCaseBuilder> caseBuilders = new HashSet<>();
    private String defaultCase;

    public ChoiceBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new ChoiceNodeImpl(qname, path);
        constraints = new ConstraintsBuilder(moduleName, line);
    }

    public ChoiceBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final ChoiceNode base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new ChoiceNodeImpl(qname, path);
        constraints = new ConstraintsBuilder(moduleName, line, base.getConstraints());

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.augmenting = base.isAugmenting();
        instance.addedByUses = base.isAddedByUses();
        instance.configuration = base.isConfiguration();
        instance.constraints = base.getConstraints();
        instance.augmentations.addAll(base.getAvailableAugmentations());

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        Set<DataSchemaNodeBuilder> wrapped = ParserUtils.wrapChildNodes(moduleName, line, new HashSet<DataSchemaNode>(
                base.getCases()), path, ns, rev, pref);
        for (DataSchemaNodeBuilder wrap : wrapped) {
            if (wrap instanceof ChoiceCaseBuilder) {
                caseBuilders.add((ChoiceCaseBuilder) wrap);
            }
        }

        instance.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public ChoiceNode build() {
        if (!isBuilt) {
            instance.setConstraints(constraints.build());
            instance.setDefaultCase(defaultCase);

            // CASES
            for (ChoiceCaseBuilder caseBuilder : caseBuilders) {
                cases.add(caseBuilder.build());
            }
            instance.addCases(cases);

            // AUGMENTATIONS
            for (AugmentationSchemaBuilder builder : augmentationBuilders) {
                augmentations.add(builder.build());
            }
            instance.addAvailableAugmentations(new HashSet<>(augmentations));

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.addUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }
        return instance;
    }

    public Set<ChoiceCaseBuilder> getCases() {
        return caseBuilders;
    }

    @Override
    public SchemaPath getPath() {
        return instance.path;
    }

    @Override
    public void setPath(SchemaPath path) {
        instance.path = path;
    }

    /**
     * Get case by name.
     *
     * @param caseName
     *            name of case to search
     * @return case with given name if present, null otherwise
     */
    public ChoiceCaseBuilder getCaseNodeByName(String caseName) {
        for (ChoiceCaseBuilder addedCase : caseBuilders) {
            if (addedCase.getQName().getLocalName().equals(caseName)) {
                return addedCase;
            }
        }
        return null;
    }

    /**
     * Add case node to this choice.
     *
     * If node is not declared with 'case' keyword, create new case builder and
     * make this node child of newly created case.
     *
     * @param caseNode
     *            case node
     */
    public void addCase(DataSchemaNodeBuilder caseNode) {
        QName caseQName = caseNode.getQName();
        String caseName = caseQName.getLocalName();

        for (ChoiceCaseBuilder addedCase : caseBuilders) {
            if (addedCase.getQName().getLocalName().equals(caseName)) {
                throw new YangParseException(caseNode.getModuleName(), caseNode.getLine(), "Can not add '" + caseNode
                        + "' to node '" + qname.getLocalName() + "' in module '" + moduleName
                        + "': case with same name already declared at line " + addedCase.getLine());
            }
        }

        if (caseNode instanceof ChoiceCaseBuilder) {
            caseBuilders.add((ChoiceCaseBuilder) caseNode);
        } else {
            ChoiceCaseBuilder caseBuilder = new ChoiceCaseBuilder(caseNode.getModuleName(), caseNode.getLine(),
                    caseQName, caseNode.getPath());
            if (caseNode.isAugmenting()) {
                // if node is added by augmentation, set case builder augmenting
                // as true and node augmenting as false
                caseBuilder.setAugmenting(true);
                caseNode.setAugmenting(false);
            }
            SchemaPath newPath = ParserUtils.createSchemaPath(caseNode.getPath(), caseQName);
            caseNode.setPath(newPath);
            caseBuilder.addChildNode(caseNode);
            caseBuilders.add(caseBuilder);
        }
    }

    @Override
    public String getDescription() {
        return instance.description;
    }

    @Override
    public void setDescription(final String description) {
        instance.description = description;
    }

    @Override
    public String getReference() {
        return instance.reference;
    }

    @Override
    public void setReference(final String reference) {
        instance.reference = reference;
    }

    @Override
    public Status getStatus() {
        return instance.status;
    }

    @Override
    public void setStatus(Status status) {
        if (status != null) {
            instance.status = status;
        }
    }

    @Override
    public boolean isAugmenting() {
        return instance.augmenting;
    }

    @Override
    public void setAugmenting(boolean augmenting) {
        instance.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return instance.addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        instance.addedByUses = addedByUses;
    }

    @Override
    public Boolean isConfiguration() {
        return instance.configuration;
    }

    @Override
    public void setConfiguration(Boolean configuration) {
        instance.configuration = configuration;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
    }

    @Override
    public void addAugmentation(AugmentationSchemaBuilder augment) {
        augmentationBuilders.add(augment);
    }

    public List<AugmentationSchemaBuilder> getAugmentationBuilders() {
        return augmentationBuilders;
    }

    public String getDefaultCase() {
        return defaultCase;
    }

    public void setDefaultCase(String defaultCase) {
        this.defaultCase = defaultCase;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChoiceBuilder other = (ChoiceBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (parentBuilder == null) {
            if (other.parentBuilder != null) {
                return false;
            }
        } else if (!parentBuilder.equals(other.parentBuilder)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "choice " + qname.getLocalName();
    }

    public final class ChoiceNodeImpl implements ChoiceNode {
        private final QName qname;
        private SchemaPath path;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private boolean augmenting;
        private boolean addedByUses;
        private boolean configuration;
        private ConstraintDefinition constraints;
        private final Set<ChoiceCaseNode> cases = new HashSet<>();
        private final Set<AugmentationSchema> augmentations = new HashSet<>();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
        private String defaultCase;

        private ChoiceNodeImpl(QName qname, SchemaPath path) {
            this.qname = qname;
            this.path = path;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public boolean isAugmenting() {
            return augmenting;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        @Override
        public boolean isConfiguration() {
            return configuration;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraints;
        }

        private void setConstraints(ConstraintDefinition constraints) {
            this.constraints = constraints;
        }

        @Override
        public Set<AugmentationSchema> getAvailableAugmentations() {
            return Collections.unmodifiableSet(augmentations);
        }

        private void addAvailableAugmentations(Set<AugmentationSchema> availableAugmentations) {
            if (availableAugmentations != null) {
                this.augmentations.addAll(availableAugmentations);
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void addUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes.addAll(unknownSchemaNodes);
            }
        }

        @Override
        public Set<ChoiceCaseNode> getCases() {
            return Collections.unmodifiableSet(cases);
        }

        @Override
        public ChoiceCaseNode getCaseNodeByName(final QName name) {
            if (name == null) {
                throw new IllegalArgumentException("Choice Case QName cannot be NULL!");
            }
            for (final ChoiceCaseNode caseNode : cases) {
                if (caseNode != null && name.equals(caseNode.getQName())) {
                    return caseNode;
                }
            }
            return null;
        }

        @Override
        public ChoiceCaseNode getCaseNodeByName(final String name) {
            if (name == null) {
                throw new IllegalArgumentException("Choice Case string Name cannot be NULL!");
            }
            for (final ChoiceCaseNode caseNode : cases) {
                if (caseNode != null && (caseNode.getQName() != null)
                        && name.equals(caseNode.getQName().getLocalName())) {
                    return caseNode;
                }
            }
            return null;
        }

        private void addCases(Set<ChoiceCaseNode> cases) {
            if (cases != null) {
                this.cases.addAll(cases);
            }
        }

        @Override
        public String getDefaultCase() {
            return defaultCase;
        }

        private void setDefaultCase(String defaultCase) {
            this.defaultCase = defaultCase;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChoiceNodeImpl other = (ChoiceNodeImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(ChoiceNodeImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=" + qname);
            sb.append("]");
            return sb.toString();
        }
    }

}
