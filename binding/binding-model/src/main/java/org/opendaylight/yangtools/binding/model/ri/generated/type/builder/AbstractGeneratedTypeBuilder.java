/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.api.AbstractType;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.Enumeration;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeComment;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

abstract class AbstractGeneratedTypeBuilder<T extends GeneratedTypeBuilderBase<T>> extends AbstractType
        implements GeneratedTypeBuilderBase<T> {
    private final @NonNull Archetype<?> archetype;

    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<Type> implementsTypes = Collections.emptyList();
    private List<Enumeration> enumDefinitions = Collections.emptyList();
    private List<Constant> constants = Collections.emptyList();
    private List<MethodSignatureBuilder> methodDefinitions = Collections.emptyList();
    private List<GeneratedTransferObject> enclosedTransferObjects = Collections.emptyList();
    private List<GeneratedPropertyBuilder> properties = Collections.emptyList();
    private TypeComment comment;
    private boolean isAbstract;
    private YangSourceDefinition yangSourceDefinition;

    protected AbstractGeneratedTypeBuilder(final Archetype<?> archetype) {
        this.archetype = requireNonNull(archetype);
    }

    @Override
    public final JavaTypeName getIdentifier() {
        return archetype.typeName();
    }

    protected final @NonNull Archetype<?> getArchetype() {
        return archetype;
    }

    protected TypeComment getComment() {
        return comment;
    }

    protected List<AnnotationTypeBuilder> getAnnotations() {
        return annotationBuilders;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return implementsTypes;
    }

    protected List<Enumeration> getEnumerations() {
        return enumDefinitions;
    }

    protected List<Constant> getConstants() {
        return constants;
    }

    @Override
    public List<MethodSignatureBuilder> getMethodDefinitions() {
        return methodDefinitions;
    }

    protected List<GeneratedTransferObject> getEnclosedTransferObjects() {
        return enclosedTransferObjects;
    }

    protected abstract T thisInstance();

    @Override
    public T addEnclosingTransferObject(final GeneratedTransferObject genTO) {
        checkArgument(genTO != null, "Parameter genTO cannot be null!");
        checkArgument(!enclosedTransferObjects.contains(genTO),
            "This generated type already contains equal enclosing transfer object.");
        enclosedTransferObjects = LazyCollections.lazyAdd(enclosedTransferObjects, genTO);
        return thisInstance();
    }

    @Override
    public T addComment(final TypeComment newComment) {
        comment = requireNonNull(newComment);
        return thisInstance();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(identifier);

        checkArgument(!annotationBuilders.contains(builder), "This generated type already contains equal annotation.");
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    @Override
    public T setAbstract(final boolean newIsAbstract) {
        isAbstract = newIsAbstract;
        return thisInstance();
    }

    @Override
    public T addImplementsType(final Type genType) {
        checkArgument(genType != null, "Type cannot be null");
        checkArgument(!implementsTypes.contains(genType),
            "This generated type already contains equal implements type.");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        checkArgument(type != null, "Returning Type for Constant cannot be null!");
        checkArgument(name != null, "Name of constant cannot be null!");
        checkArgument(!containsConstant(name),
            "This generated type already contains a \"%s\" constant", name);

        final Constant constant = new ConstantImpl(type, name, value);
        constants = LazyCollections.lazyAdd(constants, constant);
        return constant;
    }

    public boolean containsConstant(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        for (final Constant constant : constants) {
            if (name.equals(constant.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addEnumeration(final Enumeration enumeration) {
        checkArgument(enumeration != null, "Enumeration cannot be null!");

        // This enumeration may be generated from a leaf, which may end up colliding with its enclosing type
        // hierarchy. If that is the case, we use a single '$' suffix to disambiguate -- that cannot come from the user
        // and hence is marking our namespace
        checkArgument(!enumDefinitions.contains(enumeration),
            "Generated type %s already contains an enumeration for %s", this, enumeration);
        enumDefinitions = LazyCollections.lazyAdd(enumDefinitions, enumeration);
    }

    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        checkArgument(name != null, "Name of method cannot be null!");
        final MethodSignatureBuilder builder = new MethodSignatureBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        builder.setAbstract(true);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
    }

    @Override
    public boolean containsMethod(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        for (final MethodSignatureBuilder methodDefinition : methodDefinitions) {
            if (name.equals(methodDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GeneratedPropertyBuilder addProperty(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        checkArgument(!containsProperty(name),
            "This generated type already contains property with the same name.");

        final GeneratedPropertyBuilder builder = new GeneratedPropertyBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        properties = LazyCollections.lazyAdd(properties, builder);
        return builder;
    }

    @Override
    public boolean containsProperty(final String name) {
        checkArgument(name != null, "Parameter name can't be null");
        for (final GeneratedPropertyBuilder property : properties) {
            if (name.equals(property.getName())) {
                return true;
            }
        }
        return false;
    }

    public Type getParent() {
        return null;
    }

    @Override
    public List<GeneratedPropertyBuilder> getProperties() {
        return properties;
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        return Optional.ofNullable(yangSourceDefinition);
    }

    @Override
    public void setYangSourceDefinition(final YangSourceDefinition definition) {
        yangSourceDefinition = requireNonNull(definition);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return super.addToStringAttributes(toStringHelper).omitNullValues()
            .add("comment", comment == null ? null : comment.getJavadoc())
            .add("constants", constants)
            .add("enumerations", enumDefinitions)
            .add("methods", methodDefinitions)
            .add("annotations", annotationBuilders)
            .add("implements", implementsTypes);
    }
}
