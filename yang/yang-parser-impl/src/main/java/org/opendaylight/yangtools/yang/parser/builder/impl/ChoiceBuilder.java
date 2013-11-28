/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangNode;
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
    private YangNode parent;
    // DataSchemaNode args
    private boolean augmenting;
    private boolean addedByUses;
    private Boolean configuration;
    private final ConstraintsBuilder constraints;
    // AugmentationTarget args
    private final List<AugmentationSchema> augmentations = new ArrayList<>();
    private final List<AugmentationSchemaBuilder> augmentationBuilders = new ArrayList<>();
    // ChoiceNode args
    private Set<ChoiceCaseNode> cases = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
    private final Set<ChoiceCaseBuilder> caseBuilders = new HashSet<>();
    private String defaultCase;

    public ChoiceBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line, qname);
        instance = new ChoiceNodeImpl(qname);
        constraints = new ConstraintsBuilder(moduleName, line);
    }

    @Override
    public ChoiceNode build(YangNode parent) {
        if (!isBuilt) {
            this.parent = parent;
            instance.setParent(parent);
            instance.setPath(schemaPath);
            instance.setDescription(description);
            instance.setReference(reference);
            instance.setStatus(status);
            instance.setAugmenting(augmenting);
            instance.setAddedByUses(addedByUses);
            instance.setConfiguration(configuration);
            instance.setConstraints(constraints.build());
            instance.setDefaultCase(defaultCase);

            // CASES
            for (ChoiceCaseBuilder caseBuilder : caseBuilders) {
                cases.add(caseBuilder.build(instance));
            }
            instance.setCases(cases);

            // AUGMENTATIONS
            for (AugmentationSchemaBuilder builder : augmentationBuilders) {
                augmentations.add(builder.build(instance));
            }
            instance.setAvailableAugmentations(new HashSet<>(augmentations));

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build(instance));
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.setUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }
        return instance;
    }

    @Override
    public void rebuild() {
        isBuilt = false;
        build(parent);
    }

    @Override
    public void setQName(QName qname) {
        this.qname = qname;
        instance.setQName(qname);
    }

    public Set<ChoiceCaseBuilder> getCases() {
        return caseBuilders;
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
                    caseQName);
            if (caseNode.isAugmenting()) {
                // if node is added by augmentation, set case builder augmenting
                // as true and node augmenting as false
                caseBuilder.setAugmenting(true);
                caseNode.setAugmenting(false);
            }
            caseBuilder.setPath(caseNode.getPath());
            SchemaPath newPath = ParserUtils.createSchemaPath(caseNode.getPath(), caseQName);
            caseNode.setPath(newPath);
            caseBuilder.addChildNode(caseNode);
            caseBuilders.add(caseBuilder);
        }
    }

    public void setCases(Set<ChoiceCaseNode> cases) {
        this.cases = cases;
    }

    @Override
    public boolean isAugmenting() {
        return augmenting;
    }

    @Override
    public void setAugmenting(boolean augmenting) {
        this.augmenting = augmenting;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    public Boolean isConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Boolean configuration) {
        this.configuration = configuration;
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

    public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
        return addedUnknownNodes;
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
        private QName qname;
        private SchemaPath path;
        private YangNode parent;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private boolean augmenting;
        private boolean addedByUses;
        private boolean configuration;
        private ConstraintDefinition constraints;
        private Set<ChoiceCaseNode> cases = Collections.emptySet();
        private Set<AugmentationSchema> augmentations = Collections.emptySet();
        private List<UnknownSchemaNode> unknownNodes = Collections.emptyList();
        private String defaultCase;

        private ChoiceNodeImpl(QName qname) {
            this.qname = qname;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        private void setQName(QName qname) {
            this.qname = qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        private void setPath(SchemaPath path) {
            this.path = path;
        }

        @Override
        public YangNode getParent() {
            return parent;
        }

        private void setParent(YangNode parent) {
            this.parent = parent;
        }

        @Override
        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        private void setReference(String reference) {
            this.reference = reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        private void setStatus(Status status) {
            if (status != null) {
                this.status = status;
            }
        }

        @Override
        public boolean isAugmenting() {
            return augmenting;
        }

        private void setAugmenting(boolean augmenting) {
            this.augmenting = augmenting;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        private void setAddedByUses(boolean addedByUses) {
            this.addedByUses = addedByUses;
        }

        @Override
        public boolean isConfiguration() {
            return configuration;
        }

        private void setConfiguration(boolean configuration) {
            this.configuration = configuration;
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
            return augmentations;
        }

        private void setAvailableAugmentations(Set<AugmentationSchema> availableAugmentations) {
            if (availableAugmentations != null) {
                this.augmentations = availableAugmentations;
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        private void setUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes = unknownSchemaNodes;
            }
        }

        @Override
        public Set<ChoiceCaseNode> getCases() {
            return cases;
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

        private void setCases(Set<ChoiceCaseNode> cases) {
            if (cases != null) {
                this.cases = cases;
            }
        }

        @Override
        public String getDefaultCase() {
            return defaultCase;
        }

        private void setDefaultCase(String defaultCase) {
            this.defaultCase = defaultCase;
        }

        public ChoiceBuilder toBuilder() {
            return ChoiceBuilder.this;
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
