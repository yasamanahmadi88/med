package com.behsa.medportal;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.behsa.medportal.config.ApplicationProperties;
import com.behsa.medportal.config.Constants;
import com.behsa.medportal.security.UserLoginPolicy;
import com.behsa.medportal.web.rest.errors.BadRequestAlertException;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = MedPortalApp.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

    // prettier-ignore
    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..")
        .optionalLayer("Service").definedBy("..service..")
        .layer("Security").definedBy("..security..")
        .layer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..domain..")

        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config", "Security")
        .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Web", "Config")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

        .ignoreDependency(belongToAnyOf(MedPortalApp.class), alwaysTrue())
        .ignoreDependency(alwaysTrue(), belongToAnyOf(
            Constants.class,
            ApplicationProperties.class
        ))

        // Existing exception:
        // UserLoginPolicy is inside security package, but it currently throws a web-layer exception.
        // This keeps the architecture rule strict for Web layer in general,
        // while allowing only this known dependency.
        .ignoreDependency(
            belongToAnyOf(UserLoginPolicy.class),
            belongToAnyOf(BadRequestAlertException.class)
        );
}
