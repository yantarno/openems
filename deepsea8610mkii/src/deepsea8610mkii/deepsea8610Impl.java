package deepsea8610mkii;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

// 1. ИСПРАВЛЕНИЕ: Добавлен нужный импорт исключения
import io.openems.common.exceptions.OpenemsException; 
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.component.OpenemsComponent;

import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;

@Designate(ocd = Config.class, factory = true)
@Component(name = "deepsea8610mkii", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class deepsea8610Impl extends AbstractOpenemsModbusComponent implements deepsea8610mkii, OpenemsComponent {

    @Reference
    protected ConfigurationAdmin cm;

    // 2. ИСПРАВЛЕНИЕ: Добавлена критически важная аннотация для OSGi
    @Reference
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public deepsea8610Impl() {
        super(
            OpenemsComponent.ChannelId.values(),
            ModbusComponent.ChannelId.values(), // <-- Пропущенная обязательная строка
            deepsea8610mkii.ChannelId.values()
        );
    }
    

    // 3. ИСПРАВЛЕНИЕ: Добавлено "throws OpenemsException"
    @Activate
    void activate(org.osgi.service.component.ComponentContext context, Config config) throws OpenemsException {
        if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbus_id())) {
            return;
        }
    }

    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
            // Считывание режима управления. Функция 3 (Read Multiple Registers)
            new FC3ReadRegistersTask(772, Priority.LOW,
                m(deepsea8610mkii.ChannelId.CONTROL_MODE, new UnsignedWordElement(772))
            ),
            
            // Батарея и обороты находятся рядом (1029 и 1030), читаем их одним пулом задач
            // Батарея и обороты находятся рядом (1029 и 1030), читаем их одним пулом задач
            new FC3ReadRegistersTask(1029, Priority.HIGH,
                m(deepsea8610mkii.ChannelId.BATTERY_VOLTAGE, new UnsignedWordElement(1029), ElementToChannelConverter.SCALE_FACTOR_2),
                m(deepsea8610mkii.ChannelId.ENGINE_SPEED, new UnsignedWordElement(1030))
            )
        );
    }
    @Override
    public String debugLog() {
        return "DSE 8610 [" + this.id() + "]"
            + " Режим: " + this.channel(deepsea8610mkii.ChannelId.CONTROL_MODE).value().asString()
            + " | АКБ: " + this.channel(deepsea8610mkii.ChannelId.BATTERY_VOLTAGE).value().asString()
            + " | Обороты: " + this.channel(deepsea8610mkii.ChannelId.ENGINE_SPEED).value().asString();
    }
}