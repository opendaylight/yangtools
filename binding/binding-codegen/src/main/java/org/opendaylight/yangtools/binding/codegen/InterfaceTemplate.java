/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.REQUIRE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForNonnull;
import static org.opendaylight.yangtools.binding.contract.Naming.getGetterMethodForRequire;
import static org.opendaylight.yangtools.binding.contract.Naming.isGetterMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isNonnullMethodName;
import static org.opendaylight.yangtools.binding.contract.Naming.isRequireMethodName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.DocUtils;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for generating JAVA interfaces.
 */
sealed class InterfaceTemplate extends BaseTemplate permits DataRootTemplate {
    @NonNullByDefault
    record Builder(GeneratedType type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public InterfaceTemplate build() {
            return new InterfaceTemplate(type, root);
        }
    }

    private static final CharMatcher WS_MATCHER = CharMatcher.anyOf("\n\t");
    private static final Pattern SPACES_PATTERN = Pattern.compile(" +");

    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    private final List<Constant> consts;
    /**
     * List of method signatures which are generated as method declarations.
     */
    private final List<MethodSignature> methods;
    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    private final List<EnumTypeObjectArchetype> enums;
    /**
     * List of generated types which are enclosed inside the generated type.
     */
    private final List<GeneratedType> enclosedGeneratedTypes;
    private final @NonNull DataRootArchetype root;

    private @Nullable TypeAnalysis typeAnalysis;

    @NonNullByDefault
    InterfaceTemplate(final GeneratedType type, final DataRootArchetype root) {
        super(GeneratedClass.of(type), type);
        this.root = requireNonNull(root);

        consts = type.getConstantDefinitions();
        methods = type.getMethodDefinitions();
        enums = type.getEnumerations();
        enclosedGeneratedTypes = type.getEnclosedTypes();
    }

    private @NonNull TypeAnalysis typeAnalysis() {
        final var existing = typeAnalysis;
        return existing != null ? existing : loadTypeAnalysis();
    }

    private @NonNull TypeAnalysis loadTypeAnalysis() {
        final var analysis = TypeAnalysis.of(type());
        typeAnalysis = analysis;
        return analysis;
    }

    @Override
    final BlockBuilder body() {
        final var bb = newBlockBuilder()
            .blk(wrapToDocumentation(formatDataForJavaDoc(type())))
            .blk(generateAnnotations(type().getAnnotations()))
            .eol(generatedAnnotation())
            .str("public interface ").str(type().simpleName());

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
        final var ifaces = type().getImplements();
        switch (ifaces.size()) {
            case 0 -> {
                // No-op
            }
            case 1 -> bb.str(" extends ").str(importedName(ifaces.getFirst()));
            default -> {
                bb.nl().ind("extends ");

                // Note: We could try to pack multiple references into a single line, but that would require us to pick
                //       a length limit and peek into importedName to see how long it is.
                //       Perhaps it is worth the added complexity: for now this simple approach just works
                final var it = ifaces.iterator();
                while (true) {
                    bb.str(importedName(it.next()));
                    if (!it.hasNext()) {
                        break;
                    }
                    // space equivalent of 'extends'
                    bb.eol(",").ind("        ");
                }
            }
        }

        return bb
            .oB()
            .blk(generateInnerClasses(root, enclosedGeneratedTypes))
            .nl()
            .blk(generateInnerEnumTypeObjects(root, enums))
            .nl()
            .blk(generateConstants())
            .nl()
            .blk(generateMethods())
            .blk(generateJavaDataContainerMethods())
            .cB();
    }

    @Nullable BlockBuilder generateConstants() {
        if (consts.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var constant : consts) {
            // Pattern constants are emitted separately
            if (!constant.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                // FIXME: short circuit to statically-known case
                bb.txt(emitConstant(constant));
            }
        }
        return bb;
    }

    final @NonNull BlockBuilder generateDefaultImplementedInterface() {
        // Note: we cannot use importedName() or short name due to shadowing explained in MDSAL-365
        // FIXME: use selfRef()
        final var fqcn = type().canonicalName();

        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("default ").gen(importedName(CLASS), fqcn)
                .str(" " + BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME + "()").oB()
                .str("return ").str(fqcn).eol(".class;")
                .cB();
    }

    @Nullable BlockBuilder generateMethods() {
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
            } else if (method.isStatic()) {
                blk = null;
            } else if (method.getParameters().isEmpty() && isGetterMethodName(method.getName())) {
                blk = generateAccessorMethod(method);
            } else if (method.getParameters().isEmpty() && isNonnullMethodName(method.getName())) {
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
            .blk(generateJavadoc(method.getComment()))
            .blk(generateAnnotations(method.getAnnotations()))
            .str(importedReturnType(method)).sp().str(method.getName()).str("(")
                .str(generateParameters(method.getParameters())).eol(");");
    }

    private static @Nullable BlockBuilder generateJavadoc(final @Nullable TypeMemberComment comment) {
        if (comment == null) {
            return null;
        }

        // FIXME: use a BlockBuilder
        final var sb = new StringBuilder();
        final var contract = comment.contractDescription();
        if (contract != null) {
            sb.append(contract).append("\n\n");
        }
        final var reference = comment.referenceDescription();
        if (reference != null) {
            sb.append(formatReference(reference).toRawString());
        }
        final var signature = comment.typeSignature();
        if (signature != null) {
            sb.append(signature).append('\n');
        }
        if (sb.isEmpty()) {
            return null;
        }

        final var bb = Block.builder();
        appendAsJavadoc(bb, sb.toString());
        return bb;
    }

    private @Nullable BlockBuilder generateAnnotations(final @NonNull List<AnnotationType> annotations) {
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
        final var methodName = method.getName();
        if (isNonnullMethodName(methodName)) {
            return generateNonnullMethod(method);
        }
        if (isRequireMethodName(methodName)) {
            return generateRequireMethod(method);
        }
        return switch (methodName) {
            case BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME -> generateDefaultImplementedInterface();
            default ->
                JavaFileTemplate.VOID.equals(method.getReturnType().name())
                    ? generateNoopVoidInterfaceMethod(method)
                    : null;
        };
    }

    @NonNullByDefault
    private BlockBuilder generateNonnullMethod(final MethodSignature method) {
        final var ret = method.getReturnType();
        final var name = method.getName();

        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", or an empty list if it is not present"))
            .blk(generateAnnotations(method.getAnnotations()))
            .str("default ").str(importedNonNull(ret)).sp().str(name).str("()").oB()
                .str("return ").str(importedName(CODEHELPERS)).str(".nonnull(").str(getGetterMethodForNonnull(name))
                    .eol("());")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateNoopVoidInterfaceMethod(final MethodSignature method) {
        return newBlockBuilder()
            .blk(generateJavadoc(method.getComment()))
            .blk(generateAnnotations(method.getAnnotations()))
            .str("default ").str(importedName(VOID)).sp().str(method.getName()).str("(")
                .str(generateParameters(method.getParameters())).str(")").oB()
                .eol("// No-op")
            .cB();
    }

    @NonNullByDefault
    private BlockBuilder generateRequireMethod(final MethodSignature method) {
        final var name = method.getName();

        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", guaranteed to be non-null", NSEE))
            .str("default ").str(importedNonNull(method.getReturnType())).sp().str(name).str("()").oB()
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
            .str(nullableType(method.getReturnType())).sp().str(method.getName()).eol("();");
    }

    private @Nullable BlockBuilder generateAccessorAnnotations(final MethodSignature method) {
        final var annotations = method.getAnnotations();
        if (annotations.isEmpty()) {
            return null;
        }

        final var bb = newBlockBuilder();
        for (var annotation : annotations) {
            if (!Types.BOOLEAN.equals(method.getReturnType()) || !OVERRIDE.equals(annotation.name())) {
                bb.blk(generateAnnotation(annotation));
            }
        }
        return bb;
    }

    @NonNullByDefault
    private BlockBuilder generateNonnullAccessorMethod(final MethodSignature method) {
        return newBlockBuilder()
            .txt(accessorJavadoc(method, ", or an empty instance if it is not present"))
            .blk(generateAnnotations(method.getAnnotations()))
            .str(importedNonNull(method.getReturnType())).sp().str(method.getName()).eol("();");
    }

    private @Nullable BlockBuilder generateJavaDataContainerMethods() {
        if (type().getImplements().stream()
                .noneMatch(iface -> iface.name().equals(BindingTypes.JAVA_DATACONTAINER.name()))) {
            return null;
        }

        return newBlockBuilder()
            .nl()
            .blk(generateBindingHashCode())
            .nl()
            .blk(generateBindingEquals())
            .nl()
            .blk(generateBindingToString());
    }

    @VisibleForTesting
    final @NonNull BlockBuilder generateBindingHashCode() {
        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            .str("default int javaHC()").jBlock(bb -> {
                final var analysis = typeAnalysis();
                final boolean augmentable = analysis.augmentType() != null;
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
                    default -> appendBindingHashCode(bb, props, augmentable);
                }
            }).nl();
    }

    @NonNullByDefault
    private void appendBindingHashCode(final BlockBuilder bb, final Collection<BuilderGeneratedProperty> props,
            final boolean augmentable) {
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

    private @NonNull BlockBuilder generateBindingEquals() {
        final var analysis = typeAnalysis();
        final var augmentable = analysis.augmentType() != null;
        final var props = analysis.properties();

        return newBlockBuilder()
            .at().eol(importedName(OVERRIDE))
            // FIXME: selfref instead of canonicalName
            .str("default boolean javaEQ(").str(type().canonicalName()).str(" obj)").jBlock(bb -> {
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
            .str("default ").str(importedName(Types.STRING)).str(" javaTS()").jBlock(bb -> {
                final var analysis = typeAnalysis();
                final var props = analysis.properties();
                final var augmentable = analysis.augmentType() != null;

                bb.str("return ").str(importedName(CODEHELPERS));
                switch (props.size()) {
                    case 0 -> firstToStringArg(bb.str(".jcTS0("), augmentable).eol(");");
                    case 1 -> {
                        final var prop = props.iterator().next();
                        firstToStringArg(bb.str(".jcTS1("), augmentable).str(", ").jStr(prop.getName()).str(", ")
                            .str(prop.getGetterName()).eol("());");
                    }
                    default -> {
                        firstToStringArg(bb.str(".jcTSB("), augmentable).eol(")");
                        for (var prop : props) {
                            bb.ind(".prop(").jStr(prop.getName()).str(", ").str(prop.getGetterName()).eol("())");
                        }
                        bb.ind().eol(".build();");
                    }
                }
            }).nl();
    }

    private BlockBuilder firstToStringArg(final BlockBuilder bb, final boolean augmentable) {
        if (augmentable) {
            return bb.str("this");
        }
        // FIXME: use selfRef()
        return bb.str(type().canonicalName()).str(".class");
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
        final var comment = method.getComment();
        final var reference = comment == null ? null : comment.referenceDescription();
        if (reference == null) {
            return simpleAccessorJavadoc(method, orString, exception);
        }

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
