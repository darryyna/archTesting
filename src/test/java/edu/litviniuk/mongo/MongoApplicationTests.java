package edu.litviniuk.mongo;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;



@SpringBootTest
class MongoApplicationTests {

	private JavaClasses applicationClasses;
	private static final String PROJECT_PACKAGE = "edu.litviniuk.mongo";

	@BeforeEach
	void initialize() {
		applicationClasses = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages("edu.litviniuk.mongo");;
	}

	@Test
	void shouldFollowLayerArchitecture() {
		layeredArchitecture()
				.consideringAllDependencies()
				.layer("Controller").definedBy(".." + "controller" + "..")
				.layer("Service").definedBy(".." + "service" + "..")
				.layer("Repository").definedBy(".." + "repository" + "..")
				.layer("Model").definedBy(".." + "model" + "..")
				.whereLayer("Controller").mayNotBeAccessedByAnyLayer()
				.whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
				.whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
				.whereLayer("Model").mayOnlyBeAccessedByLayers("Controller", "Service", "Repository")
				.check(applicationClasses);
	}

	@Test
	void controllersShouldNotDependOnOtherControllers() {
		noClasses().that().resideInAPackage(".." + "controller" + "..")
				.should().dependOnClassesThat().resideInAPackage(".." + "controller" + "..")
				.because("Контролери не повинні залежати один від одного")
				.check(applicationClasses);
	}

	@Test
	void repositoriesShouldNotDependOnServices() {
		noClasses().that().resideInAPackage(".." + "repository" + "..")
				.should().dependOnClassesThat().resideInAPackage(".." + "service" + "..")
				.because("Репозиторії не повинні залежати від сервісів")
				.check(applicationClasses);
	}

	@Test
	void controllerClassesShouldBeNamedXController() {
		classes().that().resideInAPackage(".." + "controller" + "..")
				.should().haveSimpleNameEndingWith("Controller")
				.because("Контролери повинні мати суфікс 'Controller' у назві")
				.check(applicationClasses);
	}

	@Test
	void controllerClassesShouldBeAnnotatedByRestController() {
		classes().that().resideInAPackage(".." + "controller" + "..")
				.should().beAnnotatedWith(RestController.class)
				.because("Контролери повинні бути анотовані @RestController")
				.check(applicationClasses);
	}

	@Test
	void repositoryShouldBeInterface() {
		classes().that().resideInAPackage(".." + "repository" + "..")
				.should().beInterfaces()
				.because("Репозиторії повинні бути інтерфейсами")
				.check(applicationClasses);
	}

	@Test
	void anyControllerFieldsShouldNotBeAnnotatedAutowired() {
		noClasses().that().resideInAPackage(".." + "controller" + "..")
				.should().beAnnotatedWith(Autowired.class)
				.because("Контролери не повинні використовувати @Autowired для полів (слід використовувати конструктор)")
				.check(applicationClasses);
	}

	@Test
	void modelFieldsShouldBePrivate() {
		fields().that().areDeclaredInClassesThat().resideInAPackage(".." + "model" + "..")
				.should().haveModifier(PRIVATE)
				.because("Поля моделі повинні бути приватними")
				.check(applicationClasses);
	}

	@Test
	void serviceClassesShouldBeNamedXService() {
		classes().that().resideInAPackage(".." + "service" + "..")
				.should().haveSimpleNameEndingWith("Service")
				.because("Сервіси повинні мати суфікс 'Service' у назві")
				.check(applicationClasses);
	}

	@Test
	void serviceClassesShouldBeAnnotatedWithService() {
		classes().that().resideInAPackage(".." + "service" + "..")
				.should().beAnnotatedWith(Service.class)
				.because("Сервіси повинні бути анотовані @Service")
				.check(applicationClasses);
	}

	@Test
	void repositoryInterfacesShouldBeNamedYRepository() {
		classes().that().resideInAPackage(".." + "repository" + "..")
				.should().haveSimpleNameEndingWith("Repository")
				.because("Репозиторії повинні мати суфікс 'Repository' у назві")
				.check(applicationClasses);
	}

	@Test
	void repositoryInterfacesShouldBeAnnotatedWithRepository() {
		classes().that().resideInAPackage(".." + "repository" + "..")
				.should().beAnnotatedWith(Repository.class)
				.because("Репозиторії повинні бути анотовані @Repository")
				.check(applicationClasses);
	}

	@Test
	void controllersShouldNotAccessRepositoriesDirectly() {
		noClasses().that().resideInAPackage(".." + "controller" + "..")
				.should().dependOnClassesThat().resideInAPackage(".." + "repository" + "..")
				.because("Контролери не повинні безпосередньо використовувати репозиторії (лише через сервіси)")
				.check(applicationClasses);
	}

	@Test
	void modelClassesShouldNotDependOnOtherLayers() {
		noClasses().that().resideInAPackage(".." + "model" + "..")
				.should().dependOnClassesThat().resideInAnyPackage(".." + "controller" + "..", ".." + "service" + "..", ".." + "repository" + "..")
				.because("Моделі не повинні залежати від інших шарів")
				.check(applicationClasses);
	}

	@Test
	void classesInDefaultPackageShouldBeForbidden() {
		noClasses().that().resideOutsideOfPackage(PROJECT_PACKAGE)
				.should().resideInAPackage("")
				.because("Класи не повинні знаходитися в пакеті за замовчуванням")
				.check(applicationClasses);
	}

	@Test
	void serviceClassesShouldBePublic() {
		classes().that().resideInAPackage(".." + "service" + "..")
				.should().bePublic()
				.because("Сервіси мають бути публічними для використання іншими шарами")
				.check(applicationClasses);
	}

	@Test
	void controllersShouldNotHavePrivateMethods() {
		methods().that().areDeclaredInClassesThat().resideInAPackage(".." + "controller" + "..")
				.should().notBePrivate()
				.because("Методи в контролерах мають бути доступними")
				.check(applicationClasses);
	}

	@Test
	void servicesShouldNotDependOnControllers() {
		noClasses().that().resideInAPackage(".." + "service" + "..")
				.should().dependOnClassesThat().resideInAPackage(".." + "controller" + "..")
				.because("Сервіси не повинні залежати від контролерів")
				.check(applicationClasses);
	}

	@Test
	void modelFieldsShouldNotBePublic() {
		fields().that().areDeclaredInClassesThat().resideInAPackage(".." + "model" + "..")
				.should().notBePublic()
				.because("Поля моделей повинні бути інкапсульовані")
				.check(applicationClasses);
	}

	@Test
	void modelClassesShouldBeDocumentOrPlainPojo() {
		classes().that().resideInAPackage(".." + "model" + "..")
				.and().haveSimpleNameNotContaining("Builder")
				.should().beAnnotatedWith(Document.class)
				.orShould().haveSimpleNameEndingWith("Dto")
				.because("Моделі мають бути або Mongo документами, або DTO/POJO")
				.check(applicationClasses);
	}
}
