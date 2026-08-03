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
import static org.opendaylight.yangtools.binding.codegen.TypeNames.NSEE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.STRING;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.VOID;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForNonnull;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForRequire;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isNonnullMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isRequireMethodName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.VerifyException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.DeprecatedAnnotation;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.OverrideAnnotation;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.ContainerLikeCompat;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;

/**
 * Base class for code generators based on {@link DataContainerArchetype}.
 */
// TODO: split this class up into reusable components, i.e. use composition instead of inheritance
abstract sealed class InterfaceTemplate<T extends @NonNull DataContainerArchetype> extends ArchetypeTemplate<T>
        permits AugmentableTemplate, AugmentationTemplate, DataRootTemplate, GroupingTemplate, NotificationBodyTemplate,
                YangDataTemplate {
    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");
    private static final @NonNull ConcreteType JAVA_DATACONTAINER = ConcreteType.ofClass(JavaDataContainer.class);

    // FIXME: replace with static knowledge: for now we verify
    // "rpc" and "grouping" elements do not implement Augmentable
    private static final Set<JavaTypeName> BUILDER_INTERFACES = Set.of(
        JavaTypeName.create(Augmentable.class),
        JavaTypeName.create(Augmentation.class),
        JavaTypeName.create(EntryObject.class));

    private final @NonNull DataContainerContract contract;
    private final boolean augmentable;

    private @Nullable TypeAnalysis typeAnalysis;

    @NonNullByDefault
    InterfaceTemplate(final T archetype, final DataRootArchetype root, final DataContainerContract contract,
            final boolean augmentable) {
        super(GeneratedClass.of(archetype), archetype, root);
        this.contract = requireNonNull(contract);
        this.augmentable = augmentable;
    }

    @Nullable DataContainerArchetype builderTarget() {
        return archetype.getImplements().stream().map(Type::name).anyMatch(BUILDER_INTERFACES::contains) ? archetype
            : null;
    }

    private @NonNull TypeAnalysis typeAnalysis() {
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

        return bb
            .blk(generateMethods())
            .frg(contract.implementationIn(this))
            .cB();
    }

    // FIXME: This method forces the use of ConcreteType and ParameterizedType. Replace Type with a BlockFragment
    //        subclass (TypeFragment?) does the equivalent of JavaFileTemplate.importedName(Type)
    @NonNullByDefault
    Iterator<? extends Type> extendsTypes() {
        return archetype.getImplements().iterator();
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

    @Nullable BlockBuilder generateMethods() {
        final var methods = archetype.getMethodDefinitions();
        if (methods.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        final var it = methods.iterator();
        while (true) {
            final var method = it.next();
            final BlockBuilder blk;
            if (method.isDefault()) {
                blk = generateDefaultMethod(method);
            } else if (isGetterMethodName(method.name())) {
                blk = generateAccessorMethod(method);
            } else if (isNonnullMethodName(method.name())) {
                blk = generateNonnullAccessorMethod(method);
            } else {
                blk = generateMethod(method);
            }
            bb.blk(blk);

            if (!it.hasNext()) {
                break;
            }
            bb.newLine();
        }
        return bb;
    }

    private @NonNull BlockBuilder generateMethod(final MethodSignature method) {
        return newBlockBuilder()
            .blk(generateJavadoc(method))
            .blk(generateAnnotations(method))
            .str(importedReturnType(method)).sp().str(method.name()).eol("();");
    }

    private static @Nullable BlockBuilder generateJavadoc(final MethodSignature method) {
        final var optDescription = method.statement()
            .findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        if (optDescription.isEmpty()) {
            return null;
        }

        // FIXME: use a BlockBuilder
        final var sb = new StringBuilder();
        final var reference = optDescription.orElseThrow();
        if (reference != null) {
            sb.append(formatReference(reference).toRawString());
        }
        if (sb.isEmpty()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, sb.toString());
        return bb;
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

    private @Nullable BlockBuilder generateAnnotations(final MethodSignature method) {
        final var annotations = method.annotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var annotation : annotations) {
            bb.blk(generateAnnotation(annotation));
        }
        return bb;
    }

    private @Nullable BlockBuilder generateDefaultMethod(final MethodSignature method) {
        final var methodName = method.name();
        if (isNonnullMethodName(methodName)) {
            return generateNonnullMethod(method);
        }
        if (isRequireMethodName(methodName)) {
            return generateRequireMethod(method);
        }
        return VOID.equals(method.returnType().name()) ? generateNoopVoidInterfaceMethod(method) : null;
    }

    @NonNullByDefault
    private BlockBuilder generateNonnullMethod(final MethodSignature method) {
        final var ret = method.returnType();
        final var name = method.name();

        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", or an empty list if it is not present"))
            .blk(generateAnnotations(method))
            .str("default ").str(importedNonNull(ret)).sp().str(name).str("()").oB()
                .str("return ").str(importedName(CODEHELPERS)).str(".nonnull(").str(getGetterMethodForNonnull(name))
                    .eol("());")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateNoopVoidInterfaceMethod(final MethodSignature method) {
        return newBlockBuilder()
            .blk(generateJavadoc(method))
            .blk(generateAnnotations(method))
            .str("default ").str(importedName(VOID)).sp().str(method.name()).str("()").oB()
                .eol("// No-op")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateRequireMethod(final MethodSignature method) {
        final var name = method.name();

        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", guaranteed to be non-null", NSEE))
            .str("default ").str(importedNonNull(method.returnType())).sp().str(name).str("()").oB()
                .str("return ").str(importedName(CODEHELPERS)).str(".require(").str(getGetterMethodForRequire(name))
                    // FIXME: what exactly is this replace() doing?
                    .str("(), ").jStr(name.toLowerCase(Locale.ROOT).replace(REQUIRE_PREFIX, "")).eol(");")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateAccessorMethod(final MethodSignature method) {
        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", or {@code null} if it is not present"))
            .blk(generateAccessorAnnotations(method))
            .str(nullableType(method.returnType())).sp().str(method.name()).eol("();");
    }

    private @Nullable BlockBuilder generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.annotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var annotation : annotations) {
            // FIXME: what is this check doing?
            if (!BaseYangTypes.BOOLEAN_TYPE.equals(method.returnType())
                || !(annotation instanceof OverrideAnnotation)) {
                bb.blk(generateAnnotation(annotation));
            }
        }
        return bb;
    }

    @NonNullByDefault
    private BlockBuilder generateNonnullAccessorMethod(final MethodSignature method) {
        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", or an empty instance if it is not present"))
            .blk(generateAnnotations(method))
            .str(importedNonNull(method.returnType())).sp().str(method.name()).eol("();");
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

    // FIXME: return a Block
    @NonNullByDefault
    private String accessorJavadoc(final MethodSignature method, final String orString) {
        return accessorJavadoc(method, orString, null);
    }

    // FIXME: return a Block
    @NonNullByDefault
    private String accessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var optDescription = method.statement()
            .findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        if (optDescription.isEmpty()) {
            return simpleAccessorJavadoc(method, orString, exception);
        }

        final var reference = optDescription.orElseThrow();
        final var propName = propertyNameFromGetter(method);
        final var bb = newBlockBuilder()
            .str("Return ").str(propName).str(orString).eol(".")
            .blk(formatReference(reference))
            .nl()
            .str("@return {@code ").str(importedReturnType(method)).str("} ").str(propName).str(orString).eol(".");
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).eol(" is not present");
        }
        return bb.toJavadocBlock();
    }

    // FIXME: return a Block
    @NonNullByDefault
    private String simpleAccessorJavadoc(final MethodSignature method, final String orString,
            final @Nullable JavaTypeName exception) {
        final var propName = propertyNameFromGetter(method);

        final var bb = newBlockBuilder()
            .str("{@return {@code ").str(importedReturnType(method)).str("} ").str(propName).str(orString).eol("}");
        if (exception != null) {
            bb.str("@throws ").str(importedName(exception)).str(" if ").str(propName).eol(" is not present");
        }
        return bb.toJavadocBlock();
    }

    @NonNullByDefault
    private String nullableType(final Type type) {
        if (isObject(type) && type instanceof ParameterizedType param
            && (Types.isMapType(param) || Types.isListType(param) || Types.isSetType(param))) {
            return importedNullable(type);
        }
        return importedName(type);
    }

    // The return type has a package, so it's not a primitive type
    private static boolean isObject(final Type type) {
        return !type.packageName().isEmpty();
    }

    @NonNullByDefault
    private static BlockBuilder formatReference(final String reference) {
        final var bb = Block.builder()
            .txt("""
                <pre>
                    <code>
                """);

        // FIXME: use a {@code} block which will render some of this encoding superfluous, but it requires paying
        //        attention to '}' pairing in input
        var formattedText = DocUtils.encodeAngleBrackets(reference);
        formattedText = WS_MATCHER.replaceFrom(JavaFileTemplate.encodeJavadocSymbols(formattedText), ' ');
        formattedText = SPACES_PATTERN.matcher(formattedText).replaceAll(" ");

        // FIXME: add state keeping so that we can append direcly to BlockBuilder
        var sb = new StringBuilder();
        var isFirstElementOnNewLineEmptyChar = false;

        // FIXME: use indexOf(' ') instead of StringTokenizer
        final var tokenizer = new StringTokenizer(formattedText, " ", true);
        while (tokenizer.hasMoreTokens()) {
            final var nextElement = tokenizer.nextToken();
            final var lbLength = sb.length();

            if (lbLength != 0 && lbLength + nextElement.length() > 80) {
                final var limit = lbLength - 1;
                if (sb.charAt(limit) == ' ') {
                    sb.setLength(limit);
                }
                // FIXME: use append(CharSequence, int, int) instead
                if (!sb.isEmpty() && sb.charAt(0) == ' ') {
                    sb.deleteCharAt(0);
                }
                bb.str("        ").eol(sb.toString());
                sb.setLength(0);

                if (" ".equals(nextElement)) {
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
                }
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            } else {
                sb.append(nextElement);
            }
        }
        if (!sb.isEmpty()) {
            bb.str("        ").eol(sb.toString());
        }

        return bb
            .txt("""
                    </code>
                </pre>

                """);
    }
}
