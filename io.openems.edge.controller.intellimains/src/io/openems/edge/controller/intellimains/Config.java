package io.openems.edge.controller.intellimains;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition; 

@ObjectClassDefinition(
//		id = "comap.intellimains.basebox", 
		name = "controller intellimains basebox", 
		description = "интеграция с контроллером генератора intellimains"
)
@interface Config {
	@AttributeDefinition(name = "Component-ID", description = "Уникальный идентификатор компонента")
	String id() default "ctrlintellimains0";

	@AttributeDefinition(name = "Alias", description = "Человекочитаемое имя")
	String alias() default "";

	@AttributeDefinition(name = "Enabled", description = "Включить этот компонент")
	boolean enabled() default true;

	@AttributeDefinition(name = "Modbus-ID", description = "Идентификатор моста Modbus")
	String modbus_id() default "modbus0";

	@AttributeDefinition(name = "Modbus Unit-ID", description = "Адрес устройства в сети Modbus")
	int modbusUnitId() default 1;

	String webconsole_configurationFactory_nameHint() default "Controller IntelliMains BaseBox [{id}]";
}