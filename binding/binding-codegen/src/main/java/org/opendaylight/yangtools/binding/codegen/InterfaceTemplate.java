/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_ARRAYS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.JU_OBJECTS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.DeprecatedAnnotation;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.ContainerLikeCompat;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Base class for code generators based on {@link DataContainerArchetype}.
 */
// TODO: split this class up into reusable components, i.e. use composition instead of inheritance
abstract sealed class InterfaceTemplate<T extends @NonNull DataContainerArchetype> extends ArchetypeTemplate<T>
        permits AugmentableTemplate, AugmentationTemplate, DataRootTemplate, GroupingTemplate, NotificationBodyTemplate,
                YangDataTemplate {
    private static final @NonNull ConcreteType JAVA_DATACONTAINER = ConcreteType.ofClass(JavaDataContainer.class);

    private final @NonNull DataContainerContract contract;
    private final boolean augmentable;

    private @Nullable TypeAnalysis typeAnalysis;

    @NonNullByDefault
    InterfaceTemplate(final DataRootArchetype root, final T archetype, final DataContainerContract contract,
            final boolean augmentable) {
        super(root, archetype);
        this.contract = requireNonNull(contract);
        this.augmentable = augmentable;
    }

    final @NonNull TypeAnalysis typeAnalysis() {
        final var existing = typeAnalysis;
        return existing != null ? existing : loadTypeAnalysis();
    }

    private @NonNull TypeAnalysis loadTypeAnalysis() {
        final var analysis = TypeAnalysis.of(archetype);
        typeAnalysis = analysis;
        return analysis;
    }

    @Override
    final BlockBuilder body() {
        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc()))
            .blk(generateAnnotations())
            .eol(generatedAnnotation())
            .str("public interface ").str(archetype.simpleName());

        // We can have three shapes here to ensure reasonable separation from inner members:
        //
        //   interface Foo {
        //       int VALUE = 42;
        //
        // or
        //
        //   interface Foo extends One {
        //       int VALUE = 42;
        //
        // or
        //
        //   interface Foo
        //       extends One,
        //               Two {
        //       int VALUE = 42;
        //
        // TODO: split this out into a ExtendsKeyword, which is a BlockFragment
        // TODO: there always should be at least one interface
        final var ifaces = extendsTypes();
        if (ifaces.hasNext()) {
            final var first = ifaces.next();
            if (ifaces.hasNext()) {
                bb.nl().ind("extends ");

                // Note: We could try to pack multiple references into a single line, but that would require us to pick
                //       a length limit and peek into importedName to see how long it is.
                //       Perhaps it is worth the added complexity: for now this simple approach just works
                var current = first;
                while (true) {
                    bb.str(importedName(current));
                    if (!ifaces.hasNext()) {
                        break;
                    }
                    // space equivalent of 'extends'
                    bb.eol(",").ind("        ");
                    current = ifaces.next();
                }
            } else {
                bb.str(" extends ").str(importedName(first));
            }
        }

        bb.oB();

        final var innerClasses = generateInnerClasses(root, archetype.typeObjects());
        if (innerClasses != null) {
            bb.blk(innerClasses).newLine();
        }

        final var constants = constants();
        if (constants != null) {
            bb.frg(constants).newLine();
        }

        final var methods = archetype.getMethodDefinitions();
        if (!methods.isEmpty()) {
            bb.frg(new DataContainerGetters(this));
        }

        return bb
            .frg(contract.implementationIn(this))
            .cB();
    }

    // FIXME: This method forces the use of ConcreteType and ParameterizedType. Replace Type with a BlockFragment
    //        subclass (TypeFragment?) does the equivalent of JavaFileTemplate.importedName(Type)
    @NonNullByDefault
    Iterator<? extends Type> extendsTypes() {
        return archetype.partials().iterator();
    }

    @NonNullByDefault
    final Type extendsJavaDataContainer() {
        return ParameterizedType.of(JAVA_DATACONTAINER, archetype);
    }

    BlockFragment constants() {
        return null;
    }

    @NonNullByDefault
    private String formatDataForJavaDoc() {
        final var statement = archetype.statement();

        final var sb = new StringBuilder();
        if (statement instanceof DocumentedNode documented) {
            final var comment = DocUtils.typeCommentOf(documented);
            if (comment != null) {
                sb.append(comment.getJavadoc());
            }
        }
        YangSourceDefinition.of(root.statement(), statement).ifPresent(def -> {
            final var node = def.getNode();
            appendSnippet(sb, archetype, def.getModule(), requireEffective(node), node);
        });

        final var str = sb.toString();
        return str.isBlank() ? "" : str.stripTrailing() + '\n';
    }

    @NonNullByDefault
    private static EffectiveStatement<?, ?> requireEffective(final DocumentedNode node) {
        return switch (node) {
            case EffectiveStatementEquivalent<?> equivalent -> equivalent.asEffectiveStatement();
            case EffectiveStatement<?, ?> effective -> effective;
            case ContainerLikeCompat compat -> requireEffective(compat.delegate());
            default -> throw new VerifyException("Unsupported node " + node);
        };
    }

    private @Nullable BlockBuilder generateAnnotations() {
        if (archetype.statement() instanceof DocumentedNode.WithStatus withStatus) {
            final var annotation = DeprecatedAnnotation.ofStatus(withStatus.getStatus());
            if (annotation != null) {
                return generateAnnotation(annotation);
            }
        }
        return null;
    }

    @VisibleForTesting
    final @NonNull BlockBuilder generateBindingHashCode() {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("default int javaHC()").jBlock(bb -> {
                final var analysis = typeAnalysis();
                final var props = analysis.properties();

                switch (props.size()) {
                    case 0 -> {
                        if (augmentable) {
                            bb.str("return ").str(importedName(CODEHELPERS)).eol(".jcHC0(this);");
                        } else {
                            bb.eol("return 1;");
                        }
                    }
                    case 1 -> {
                        final var property = props.iterator().next();
                        bb.str("return ").str(importedName(CODEHELPERS)).str(".jcHC1(");
                        if (augmentable) {
                            bb.str("this, ");
                        }
                        bb.str(getterMethodName(property)).eol("());");
                    }
                    // TODO: consider specializing for N=2 (sngle line) for the cost of 8 new methods in CodeHelpers
                    default -> appendBindingHashCode(bb, props);
                }
            }).nl();
    }

    @NonNullByDefault
    private void appendBindingHashCode(final BlockBuilder bb, final Collection<BuilderGeneratedProperty> props) {
        // determine the composition of properties: 'type binary' fields map to byte[] and therefore have to be hashed
        // via Arrays.hashCode(), not Objects.hashCode()
        final int size = props.size();
        final boolean[] isBinary = new boolean[size];
        int cnt = 0;
        int binaryCount = 0;
        for (var prop : props) {
            final var tmp = prop.getReturnType().isArray();
            if (tmp) {
                binaryCount++;
            }
            isBinary[cnt++] = tmp;
        }

        // either all are byte[] or none are: we can use CodeHelpers.jcHCN()
        final boolean useN = binaryCount == 0 || binaryCount == size;

        bb.str("return ").str(importedName(CODEHELPERS)).str(useN ? ".jcHCN(" : ".jcHC(");
        if (augmentable) {
            bb.eol("this,");
        } else {
            bb.newLine();
        }

        final var it = props.iterator();
        if (useN) {
            appendBindingHashCodeArgs(bb, it);
        } else {
            appendBindingHashCodeArgs(bb, it, isBinary);
        }
        bb.eol(");");
    }

    // all properties are the same: just pass them down to CodeHelpers
    private static void appendBindingHashCodeArgs(final BlockBuilder bb, final Iterator<BuilderGeneratedProperty> it) {
        while (true) {
            final var prop = it.next();
            bb.ind(getterMethodName(prop)).str("()");
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
    }

    // we have at least one Object and one byte[] property: compute their hashCode() ourselves
    private void appendBindingHashCodeArgs(final BlockBuilder bb, final Iterator<BuilderGeneratedProperty> it,
            final boolean[] isBinary) {
        final var arrays = importedName(JU_ARRAYS);
        final var objects = importedName(JU_OBJECTS);

        int cnt = 0;
        while (true) {
            final var prop = it.next();
            bb.ind(isBinary[cnt++] ? arrays : objects).str(".hashCode(").str(getterMethodName(prop)).str("())");
            if (!it.hasNext()) {
                break;
            }
            bb.eol(",");
        }
    }

    @NonNull BlockBuilder generateBindingEquals() {
        final var props = typeAnalysis().properties();

        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            // FIXME: selfref instead of canonicalName
            .str("default boolean javaEQ(").str(archetype.canonicalName()).str(" obj)").jBlock(bb -> {
                if (props.isEmpty() && !augmentable) {
                    bb.str(importedName(JU_OBJECTS)).eol(".requireNonNull(obj);");
                    bb.eol("return true;");
                    return;
                }

                bb.str("return ");
                boolean notFirst = false;
                for (var property : ByTypeMemberComparator.sort(props)) {
                    if (notFirst) {
                        bb.nl().ind("&& ");
                    } else {
                        notFirst = true;
                    }

                    final var getterName = property.getGetterName();
                    bb.str(importedUtilClass(property)).str(".equals(").str(getterName).str("(), obj.")
                        .str(getterName).str("())");
                }
                if (augmentable) {
                    if (notFirst) {
                        bb.nl().ind("&& ");
                    } else {
                        notFirst = true;
                    }
                    bb.str("augmentations().equals(obj.augmentations())");
                }

                bb.eS();
            }).nl();
    }

    @VisibleForTesting
    final BlockBuilder generateBindingToString() {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("default ").str(importedName(STRING)).str(" javaTS()").jBlock(bb -> {
                final var props = typeAnalysis().properties();

                bb.str("return ").str(importedName(CODEHELPERS));
                switch (props.size()) {
                    case 0 -> firstToStringArg(bb.str(".jcTS0(")).eol(");");
                    case 1 -> {
                        final var prop = props.iterator().next();
                        firstToStringArg(bb.str(".jcTS1(")).str(", ").jStr(prop.getName()).str(", ")
                            .str(prop.getGetterName()).eol("());");
                    }
                    default -> {
                        firstToStringArg(bb.str(".jcTSB(")).eol(")");
                        for (var prop : props) {
                            bb.ind(".prop(").jStr(prop.getName()).str(", ").str(prop.getGetterName()).eol("())");
                        }
                        bb.ind().eol(".build();");
                    }
                }
            }).nl();
    }

    private BlockBuilder firstToStringArg(final BlockBuilder bb) {
        if (augmentable) {
            return bb.str("this");
        }
        // FIXME: use selfRef()
        return bb.str(archetype.canonicalName()).str(".class");
    }
}
