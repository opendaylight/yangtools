/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.ri.Types.STRING;
import static org.opendaylight.mdsal.binding.model.ri.Types.classType;
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveBooleanType;
import static org.opendaylight.mdsal.binding.model.ri.Types.primitiveIntType;
import static org.opendaylight.mdsal.binding.model.ri.Types.wildcardTypeFor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 *
 * <p>
 * Each tree is rooted in a {@link ModuleGenerator} and its organization follows roughly YANG {@code schema tree}
 * layout, but with a twist coming from the reuse of generated interfaces from a {@code grouping} in the location of
 * every {@code uses} encountered and also the corresponding backwards propagation of {@code augment} effects.
 *
 * <p>
 * Overall the tree layout guides the allocation of Java package and top-level class namespaces.
 */
public abstract class Generator implements Iterable<Generator> {
    static final JavaTypeName DEPRECATED_ANNOTATION = JavaTypeName.create(Deprecated.class);
    static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    private final AbstractCompositeGenerator<?, ?> parent;

    private Optional<Member> member;
    private GeneratorResult result;
    private JavaTypeName typeName;
    private String javaPackage;

    Generator() {
        parent = null;
    }

    Generator(final AbstractCompositeGenerator<?, ?> parent) {
        this.parent = requireNonNull(parent);
    }

    public final @NonNull Optional<GeneratedType> generatedType() {
        return Optional.ofNullable(result.generatedType());
    }

    public @NonNull List<GeneratedType> auxiliaryGeneratedTypes() {
        return List.of();
    }

    @Override
    public Iterator<Generator> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Return the {@link AbstractCompositeGenerator} inside which this generator is defined. It is illegal to call this
     * method on a {@link ModuleGenerator}.
     *
     * @return Parent generator
     */
    final @NonNull AbstractCompositeGenerator<?, ?> getParent() {
        return verifyNotNull(parent, "No parent for %s", this);
    }

    boolean isEmpty() {
        return true;
    }

    /**
     * Return the namespace of this statement.
     *
     * @return Corresponding namespace
     * @throws UnsupportedOperationException if this node does not have a corresponding namespace
     */
    abstract @NonNull StatementNamespace namespace();

    @NonNull ModuleGenerator currentModule() {
        return getParent().currentModule();
    }

    /**
     * Push this statement into a {@link SchemaInferenceStack} so that the stack contains a resolvable {@code data tree}
     * hierarchy.
     *
     * @param inferenceStack Target inference stack
     */
    abstract void pushToInference(@NonNull SchemaInferenceStack inferenceStack);

    abstract @NonNull ClassPlacement classPlacement();

    final @NonNull Member getMember() {
        return verifyNotNull(ensureMember(), "No member for %s", this);
    }

    final Member ensureMember() {
        if (member == null) {
            member = switch (classPlacement()) {
                case NONE -> Optional.empty();
                case MEMBER, PHANTOM, TOP_LEVEL -> Optional.of(createMember(parentDomain()));
            };
        }
        return member.orElse(null);
    }

    @NonNull CollisionDomain parentDomain() {
        return getParent().domain();
    }

    abstract @NonNull Member createMember(@NonNull CollisionDomain domain);

    /**
     * Create the type associated with this builder. This method idempotent.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    final void ensureType(final TypeBuilderFactory builderFactory) {
        if (result != null) {
            return;
        }

        result = switch (classPlacement()) {
            case NONE, PHANTOM -> GeneratorResult.empty();
            case MEMBER -> GeneratorResult.member(createTypeImpl(requireNonNull(builderFactory)));
            case TOP_LEVEL -> GeneratorResult.toplevel(createTypeImpl(requireNonNull(builderFactory)));
        };

        for (Generator child : this) {
            child.ensureType(builderFactory);
        }
    }

    @NonNull GeneratedType getGeneratedType(final TypeBuilderFactory builderFactory) {
        return verifyNotNull(tryGeneratedType(builderFactory), "No type generated for %s", this);
    }

    final @Nullable GeneratedType tryGeneratedType(final TypeBuilderFactory builderFactory) {
        ensureType(builderFactory);
        return result.generatedType();
    }

    final @Nullable GeneratedType enclosedType(final TypeBuilderFactory builderFactory) {
        ensureType(builderFactory);
        return result.enclosedType();
    }

    /**
     * Create the type associated with this builder, as per {@link #ensureType(TypeBuilderFactory)} contract. This
     * method is guaranteed to be called at most once.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     */
    abstract @NonNull GeneratedType createTypeImpl(@NonNull TypeBuilderFactory builderFactory);

    final @NonNull String assignedName() {
        return getMember().currentClass();
    }

    final @NonNull String javaPackage() {
        String local = javaPackage;
        if (local == null) {
            javaPackage = local = createJavaPackage();
        }
        return local;
    }

    @NonNull String createJavaPackage() {
        final String parentPackage = getPackageParent().javaPackage();
        final String myPackage = getMember().currentPackage();
        return BindingMapping.normalizePackageName(parentPackage + '.' + myPackage);
    }

    final @NonNull JavaTypeName typeName() {
        JavaTypeName local = typeName;
        if (local == null) {
            typeName = local = createTypeName();
        }
        return local;
    }

    @NonNull JavaTypeName createTypeName() {
        return JavaTypeName.create(getPackageParent().javaPackage(), assignedName());
    }

    @NonNull AbstractCompositeGenerator<?, ?> getPackageParent() {
        return getParent();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }

    final void addImplementsChildOf(final GeneratedTypeBuilder builder) {
        AbstractCompositeGenerator<?, ?> ancestor = getParent();
        while (true) {
            // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
            if (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
                ancestor = ancestor.getParent();
                continue;
            }

            // if we into a choice we need to follow the hierararchy of that choice
            if (ancestor instanceof AbstractAugmentGenerator augment
                && augment.targetGenerator() instanceof ChoiceGenerator targetChoice) {
                ancestor = targetChoice;
                continue;
            }

            break;
        }

        builder.addImplementsType(BindingTypes.childOf(Type.of(ancestor.typeName())));
    }

    /**
     * Add common methods implemented in a generated type. This includes {@link DataContainer#implementedInterface()} as
     * well has {@code bindingHashCode()}, {@code bindingEquals()} and {@code bindingToString()}.
     *
     * @param builder Target builder
     */
    static final void addConcreteInterfaceMethods(final GeneratedTypeBuilder builder) {
        defaultImplementedInterace(builder);

        builder.addMethod(BindingMapping.BINDING_HASHCODE_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveIntType());
        builder.addMethod(BindingMapping.BINDING_EQUALS_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveBooleanType());
        builder.addMethod(BindingMapping.BINDING_TO_STRING_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(STRING);
    }

    static final void annotateDeprecatedIfNecessary(final EffectiveStatement<?, ?> stmt,
            final AnnotableTypeBuilder builder) {
        if (stmt instanceof WithStatus withStatus) {
            annotateDeprecatedIfNecessary(withStatus, builder);
        }
    }

    static final void annotateDeprecatedIfNecessary(final WithStatus node, final AnnotableTypeBuilder builder) {
        switch (node.getStatus()) {
            case DEPRECATED ->
                // FIXME: we really want to use a pre-made annotation
                builder.addAnnotation(DEPRECATED_ANNOTATION);
            case OBSOLETE -> builder.addAnnotation(DEPRECATED_ANNOTATION).addParameter("forRemoval", "true");
            case CURRENT -> {
                // No-op
            }
            default -> throw new IllegalStateException("Unhandled status in " + node);
        }
    }

    static final void addUnits(final GeneratedTOBuilder builder, final TypeDefinition<?> typedef) {
        typedef.getUnits().ifPresent(units -> {
            if (!units.isEmpty()) {
                builder.addConstant(Types.STRING, "_UNITS", "\"" + units + "\"");
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("UNITS");
                prop.setReturnType(Types.STRING);
                builder.addToStringProperty(prop);
            }
        });
    }

    /**
     * Add {@link java.io.Serializable} to implemented interfaces of this TO. Also compute and add serialVersionUID
     * property.
     *
     * @param builder transfer object which needs to be made serializable
     */
    static final void makeSerializable(final GeneratedTOBuilder builder) {
        builder.addImplementsType(Types.serializableType());
        addSerialVersionUID(builder);
    }

    static final void addSerialVersionUID(final GeneratedTOBuilder gto) {
        final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
        prop.setValue(Long.toString(SerialVersionHelper.computeDefaultSUID(gto)));
        gto.setSUID(prop);
    }

    /**
     * Add a {@link DataContainer#implementedInterface()} declaration with a narrower return type to specified builder.
     *
     * @param builder Target builder
     */
    static final void narrowImplementedInterface(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, wildcardTypeFor(builder.getIdentifier()));
    }

    /**
     * Add a default implementation of {@link DataContainer#implementedInterface()} to specified builder.
     *
     * @param builder Target builder
     */
    static final void defaultImplementedInterace(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, Type.of(builder)).setDefault(true);
    }

    static final <T extends EffectiveStatement<?, ?>> AbstractExplicitGenerator<T, ?> getChild(final Generator parent,
            final Class<T> type) {
        for (Generator child : parent) {
            if (child instanceof AbstractExplicitGenerator) {
                @SuppressWarnings("unchecked")
                final AbstractExplicitGenerator<T, ?> explicit = (AbstractExplicitGenerator<T, ?>)child;
                if (type.isInstance(explicit.statement())) {
                    return explicit;
                }
            }
        }
        throw new IllegalStateException("Cannot find " + type + " in " + parent);
    }

    private static MethodSignatureBuilder defineImplementedInterfaceMethod(final GeneratedTypeBuilder typeBuilder,
            final Type classType) {
        final MethodSignatureBuilder ret = typeBuilder
                .addMethod(BindingMapping.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME)
                .setAccessModifier(AccessModifier.PUBLIC)
                .setReturnType(classType(classType));
        ret.addAnnotation(OVERRIDE_ANNOTATION);
        return ret;
    }
}
