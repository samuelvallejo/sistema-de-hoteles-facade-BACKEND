package com.hotel;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
    @Test
    void controllersCannotBypassFacade() {
        var classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.hotel");
        noClasses().that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..", "..factory..", "..strategy..")
                .check(classes);
    }

    @Test
    void subsystemsDoNotDependOnEachOtherOrFacade() {
        var classes = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.hotel");
        for (String name : new String[]{"RoomService", "RateService", "AdditionalServiceService", "BillingService", "AccessService"}) {
            noClasses().that().haveSimpleName(name).should().dependOnClassesThat()
                    .resideInAPackage("..facade..").check(classes);
            for (String other : new String[]{"RoomService", "RateService", "AdditionalServiceService", "BillingService", "AccessService"}) {
                if (!name.equals(other)) {
                    noClasses().that().haveSimpleName(name).should().dependOnClassesThat()
                            .haveSimpleName(other).check(classes);
                }
            }
        }
    }
}
