/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.PACKAGE_PREFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.getClassName;
import static org.opendaylight.yangtools.binding.contract.Naming.getModelRootPackageName;
import static org.opendaylight.yangtools.binding.contract.Naming.getRootPackageName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;

/**
 * Template for {@link YangModuleInfo} implementation for a particular module. Aside from fulfilling that contract,
 * this class provides a static {@code createQName(String)} method, which is used by co-generated code to initialize
 * QNAME constants.
 */
public final class YangModuleInfoTemplate {
    /**
     * The name of the static field holding the {@link UnsafeAccess} instance.
     */
    static final @NonNull String CONST_UNSAFE_ACCESS = "UNSAFE_ACCESS";
    /**
     * The name of the static field holding the {@link ScalarTypeObjectRegistrar} instance.
     */
    static final @NonNull String CONST_STO_REGISTRAR = "STO_REGISTRAR";

    /**
     * The name of the {@link YangModuleInfo} implementation class.
     */
    static final @NonNull String CLASS_NAME = "YangModuleInfoImpl";
    /**
     * The name of the {@link YangModelBindingProvider} implementation class.
     */
    static final @NonNull String MODEL_BINDING_PROVIDER_CLASS_NAME = "YangModelBindingProviderImpl";
    /**
     * The name of the field holding the {@link YangModuleInfo} instance.
     */
    static final @NonNull String INSTANCE_FIELD_NAME = "INSTANCE";
    /**
     * The name of the {@link QName} factory method.
     */
    static final @NonNull String QNAMEOF_METHOD_NAME = "qnameOf";
    /**
     * The name of the {@link YangDataName} factory method.
     */
    static final @NonNull String YANGDATANAMEOF_METHOD_NAME = "yangDataNameOf";

    /**
     * The root package hierarchy of all classes generated for the purposes of discovering and loading YANG modules
     * along with their semantics. Each module is assigned a its own sub-hierarchy based on its {@code namespace} and
     * {@code revision}.
     */
    private static final @NonNull String SVC_PACKAGE_PREFIX = "org.opendaylight.yang.svc.v1";

    // These are always imported. Note we need to import even java.lang members, as there can be conflicting definitions
    // in our package
    private static final String CORE_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
        import org.opendaylight.yangtools.binding.lib.UnsafeAccessSupport;
        import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private static final String EXT_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import java.util.HashSet;
        import java.util.Set;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
        import org.opendaylight.yangtools.binding.lib.UnsafeAccessSupport;
        import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private final @NonNull Function<ModuleLike, List<String>> moduleFilePathResolver;
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull Module module;
    private final @NonNull String packageName;
    private final @NonNull String modelBindingProviderName;
    private final boolean hasYangData;

    private String importedTypes = CORE_IMPORT_STR;

    public YangModuleInfoTemplate(final Module module, final EffectiveModelContext modelContext,
            final Function<ModuleLike, List<String>> moduleFilePathResolver) {
        this.module = requireNonNull(module);
        this.modelContext = requireNonNull(modelContext);
        this.moduleFilePathResolver = requireNonNull(moduleFilePathResolver);
        packageName = servicePackageName(module.getQNameModule());
        modelBindingProviderName = packageName + '.' + MODEL_BINDING_PROVIDER_CLASS_NAME;
        hasYangData = module.asEffectiveStatement().findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .isPresent();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @NonNullByDefault
    static JavaTypeName nameInModuleOf(final GeneratedType genType) {
        // Yeah: not pretty but works
        return JavaTypeName.create(rootToService(genType.packageName()), CLASS_NAME);
    }

    @NonNullByDefault
    static JavaTypeName yangModuleInfoOf(final QNameModule module) {
        return JavaTypeName.create(servicePackageName(module), CLASS_NAME);
    }

    /**
     * Return the package name for placing generated ServiceLoader entities.
     *
     * @param module module namespace
     * @return the package name for placing generated ServiceLoader entities
     */
    @NonNullByDefault
    static String servicePackageName(final QNameModule module) {
        return rootToService(getRootPackageName(module));
    }

    @NonNullByDefault
    private static String rootToService(final String packageName) {
        return getModelRootPackageName(packageName).replace(PACKAGE_PREFIX, SVC_PACKAGE_PREFIX);
    }

    @NonNull String modelBindingProviderName() {
        return modelBindingProviderName;
    }

    @NonNull String packageName() {
        return packageName;
    }

    public String generate() {
        final var submodules = new LinkedHashSet<Submodule>();
        collectSubmodules(submodules, module);

        var bb = Block.builder()
            .eol("/**")
            .str(" * The {@link ResourceYangModuleInfo} for {@code ").str(module.getName()).eol("} module.")
            .eol(" */")
            .eol("@javax.annotation.processing.Generated(\"mdsal-binding-generator\")")
            .str("public final class " + CLASS_NAME + " extends ResourceYangModuleInfo").oB()
                .str("private static final @NonNull QName NAME = QName.create(")
                    .jStr(module.getQNameModule().namespace().toString()).str(", ");
        module.getRevision().ifPresent(revision -> bb.jStr(revision.toString()).str(", "));
        bb
            .jStr(module.getName()).eol(").intern();")
            .txt("""

                  /**
                   * The singleton instance.
                   */
                  """)
            .eol("public static final @NonNull YangModuleInfo " + INSTANCE_FIELD_NAME + " = new " + CLASS_NAME + "();")
            .txt("""

                /**
                 * The {@link ScalarTypeObjectRegistrar} instance. Exposed for technical reasons: this field should only
                 * be referenced from a static initialization block of a generated ScalarTypeObject class.
                 */
                """)
            .str("public static final @NonNull ScalarTypeObjectRegistrar " + CONST_STO_REGISTRAR).eS()
            .txt("""

                  /**
                   * The {@link UnsafeAccess} instance. Exposed for technical reasons: use this module's DataRoot's
                   * {@code META} field and acquire an instance through its {@code unsafeAccess()} method instead.
                   */
                  """)
            .str("public static final @NonNull UnsafeAccess " + CONST_UNSAFE_ACCESS).eS()
            .nl()
            .str("static").jBlock(si -> {
                si
                    .eol("final var support = UnsafeAccessSupport.of(")
                    .ind().jStr(getRootPackageName(module.getQNameModule())).eol(",")
                    .ind().eol(CLASS_NAME + ".class.getModule());")
                    .eol(CONST_UNSAFE_ACCESS + " = support.access();")
                    .eol(CONST_STO_REGISTRAR + " = support.stoRegistrar();");
            }).nl()
            .nl()
            .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
            .nl()
            .blk(classBody(module, CLASS_NAME, submodules))
            .nl()
            .txt("""
                  /**
                   * Create an interned {@link QName} with specified {@code localName} and namespace/revision of this
                   * module.
                   *
                   * @param localName local name
                   * @return A QName
                   * @throws NullPointerException if {@code localName} is {@code null}
                   * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier
                   */
                  """)
            .str("public static @NonNull QName " + QNAMEOF_METHOD_NAME + "(final String localName)").oB()
                .eol("return QName.create(NAME, localName).intern();")
            .cB();

        if (hasYangData) {
            bb
                .txt("""

                      /**
                       * Create an interned {@link YangDataName} with specified {@code templateName} and \
                      namespace/revision of
                       * this module.
                       *
                       * @param templateName template name
                       * @return A YangDataName
                       * @throws NullPointerException if {@code templateName} is {@code null}
                       * @throws IllegalArgumentException if {@code templateName} is empty
                       */
                      """)
                .str("public static @NonNull YangDataName " + YANGDATANAMEOF_METHOD_NAME
                    + "(final String templateName)").oB()
                    .eol("return new YangDataName(NAME.getModule(), templateName).intern();")
                .cB();
        }
        final var body = bb.cB().toRawString();

        final var sb = new StringBuilder()
            .append("package ").append(packageName).append(";\n")
            .append('\n')
            .append(importedTypes);
        if (hasYangData) {
            sb.append("import org.opendaylight.yangtools.yang.common.YangDataName;\n");
        }
        return sb
            .append('\n')
            .append(body)
            .toString();
    }

    public String generateModelProvider() {
        return "package " + packageName + ";\n"
            +  """

                import java.lang.Override;
                import java.util.ServiceLoader;
                import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
                import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

                /**
                """
            +  " * The {@link YangModelBindingProvider} for {@code " + module.getName()
                + "} module. This class should not be used\n"
            +  " * directly, but rather through {@link ServiceLoader}.\n"
            +  " */\n"
            +  '@' + JavaFileTemplate.GENERATED + "(\"mdsal-binding-generator\")\n"
            +  "public final class " + MODEL_BINDING_PROVIDER_CLASS_NAME + " implements YangModelBindingProvider {\n"
            +  """
                    /**
                     * Construct a new provider.
                     */
                """
            +  "    public " + MODEL_BINDING_PROVIDER_CLASS_NAME + "() {\n"
            +  "        // Nothing else\n"
            +  "    }\n"
            +  '\n'
            +  "    @Override\n"
            +  "    public YangModuleInfo getModuleInfo() {\n"
            +  "        return " + CLASS_NAME + '.' + INSTANCE_FIELD_NAME + ";\n"
            +  "    }\n"
            +  "}\n";
    }

    private static void collectSubmodules(final LinkedHashSet<Submodule> dest, final ModuleLike module) {
        for (var submodule : module.getSubmodules()) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule);
            }
        }
    }

    @NonNullByDefault
    private BlockBuilder classBody(final ModuleLike mod, final String className, final Set<Submodule> submodules) {
        final var bb = Block.builder()
            .str("private ").str(className).str("()").oB();

        if (!mod.getImports().isEmpty() || !submodules.isEmpty()) {
            importedTypes = YangModuleInfoTemplate.EXT_IMPORT_STR;
            bb.eol("Set<YangModuleInfo> set = new HashSet<>();");
        }

        for (var imp : mod.getImports()) {
            final var name = imp.getModuleName().getLocalName();
            final var optRrev = imp.getRevision();

            final QNameModule qnameModule;
            if (optRrev.isEmpty()) {
                final var sorted = new TreeMap<RevisionUnion, Module>();
                for (var m : modelContext.getModules()) {
                    if (name.equals(m.getName())) {
                        sorted.put(m.getQNameModule().revisionUnion(), m);
                    }
                }
                qnameModule = sorted.lastEntry().getValue().getQNameModule();
            } else {
                qnameModule = modelContext.findModule(name, optRrev).orElseThrow().getQNameModule();
            }

            bb.str("set.add(").str(servicePackageName(qnameModule))
                .eol('.' + CLASS_NAME + '.' + INSTANCE_FIELD_NAME + ");");
        }

        for (var submodule : submodules) {
            bb.str("set.add(").str(getClassName(submodule.getName())).eol("Info." + INSTANCE_FIELD_NAME + ");");
        }

        if (mod.getImports().isEmpty() && submodules.isEmpty()) {
            bb.eol("importedModules = ImmutableSet.of();");
        } else {
            bb.eol("importedModules = ImmutableSet.copyOf(set);");
        }

        final var pathItems = moduleFilePathResolver.apply(mod);
        if (pathItems.isEmpty()) {
            throw new IllegalStateException("Module " + mod + " does not have a file path");
        }

        bb
            .cB()
            .txt("""

                  @Override
                  public QName name() {
                      return NAME;
                  }

                  @Override
                  protected String resourceName() {
                  """)
                .str("    return \"");
        for (var pathItem : pathItems) {
            bb.str("/").str(pathItem);
        }
        bb
            .eol("\";")
            .txt("""
                  }

                  @Override
                  public ImmutableSet<YangModuleInfo> getImportedModules() {
                      return importedModules;
                  }
                  """);

        for (var sub : submodules) {
            final var subName = getClassName(sub.getName());

            bb
                .nl()
                .str("private static final class ").str(subName).str("Info extends ResourceYangModuleInfo").oB()
                    .str("private final @NonNull QName NAME = QName.create(")
                        .jStr(sub.getQNameModule().namespace().toString()).str(", ");
            sub.getRevision().ifPresent(rev -> bb.jStr(rev.toString()).str(", "));
            bb
                .jStr(sub.getName()).eol(").intern();")
                .nl()
                .str("static final @NonNull YangModuleInfo INSTANCE = new ").str(subName).eol("Info();")
                .nl()
                .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
                .nl()
                .blk(classBody(sub, subName + "Info", Set.of()))
                .cB();
        }

        return bb;
    }
}
