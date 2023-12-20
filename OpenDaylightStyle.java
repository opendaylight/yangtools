@Value.Style(
    depluralize = true,
    stagedBuilder = true,
    strictBuilder = true,
    visibility = ImplementationVisibility.PRIVATE,
    builderVisibility = BuilderVisibility.PACKAGE,
    allowedClasspathAnnotations = { SuppressWarnings.class, Generated.class })
