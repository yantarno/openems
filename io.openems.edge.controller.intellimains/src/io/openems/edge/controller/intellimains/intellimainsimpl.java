package io.openems.edge.controller.intellimains;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.ElementToChannelConverter;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.SignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC16WriteRegistersTask;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.channel.ChannelId;
import io.openems.edge.common.channel.WriteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.openems.common.exceptions.OpenemsException;


@Designate(ocd = Config.class, factory = true)
@Component(
		name = "ComAp.IntelliMains.BaseBox", 
		immediate = true, 
		configurationPolicy = ConfigurationPolicy.REQUIRE
)
public class intellimainsimpl extends AbstractOpenemsModbusComponent implements intellimains, OpenemsComponent, ModbusComponent  {
	
    // Адрес регистра управления. Смещение 156 (RemoteControl)
    private final UnsignedWordElement controlCommandElement = new UnsignedWordElement(156);

            // Добавьте эту строку (замените IntellimainsImpl на актуальное имя вашего класса реализации)
        private final Logger log = LoggerFactory.getLogger(intellimainsimpl.class);

        // Дальше идет ваш конструктор, метод activate() и т.д.
    
    
    @Reference
    protected ConfigurationAdmin cm;

    @Reference
    protected void setModbus(BridgeModbus modbus) {
        super.setModbus(modbus);
    }

    public intellimainsimpl() {
        super(
            OpenemsComponent.ChannelId.values(), 
            ModbusComponent.ChannelId.values(), 
            intellimains.ChannelId.values() 
        );
    }

    @Activate
    void activate(ComponentContext context, Config config) throws OpenemsException {
        if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbus_id())) {
            return;
        }

        // Подписываемся на события записи для команд управления
     // Подписываемся на события записи для команд управления

     // Подписываемся на события записи для команд управления
        WriteChannel<Integer> controlModeChannel = this.channel(intellimains.ChannelId.SET_CONTROL_MODE);
        controlModeChannel.onSetNextWrite(value -> {
            if (value != null) {
                this.controlCommandElement.setNextWriteValue(((Number) value).intValue());
                this.logInfo(this.log, "Отправлена команда управления: " + value);
            }
        });
        }

    @Deactivate
    protected void deactivate() {
        super.deactivate();
    }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
            // Блок 1: датчики двигателя (40 013–40 019) — смещение 12–18
            new FC3ReadRegistersTask(12, Priority.HIGH,
                m(intellimains.ChannelId.BATTERY_VOLTAGE, new UnsignedWordElement(12)),
                new DummyRegisterElement(13, 15),
                m(intellimains.ChannelId.OIL_PRESSURE, new SignedWordElement(16)),
                new DummyRegisterElement(17),
                m(intellimains.ChannelId.COOLANT_TEMPERATURE, new SignedWordElement(18)) 
            ),

            // Блок 2: статусы и режимы (40 157–40 168) — смещение 156–167
            new FC3ReadRegistersTask(156, Priority.LOW,
                new DummyRegisterElement(156, 161),
                m(intellimains.ChannelId.ENGINE_STATE, new UnsignedWordElement(162)),     
                new DummyRegisterElement(163, 166),
                m(intellimains.ChannelId.CONTROLLER_MODE, new UnsignedWordElement(167))   
            ),

            // Блок 3: электрические параметры генератора (40 249–40 274) — смещение 248–273
            new FC3ReadRegistersTask(248, Priority.HIGH,
                m(intellimains.ChannelId.GEN_L1_N_VOLTAGE, new UnsignedWordElement(248)), 
                m(intellimains.ChannelId.GEN_L2_N_VOLTAGE, new UnsignedWordElement(249)), 
                m(intellimains.ChannelId.GEN_L3_N_VOLTAGE, new UnsignedWordElement(250)), 
                new DummyRegisterElement(251),
                m(intellimains.ChannelId.GEN_L1_L2_VOLTAGE, new UnsignedWordElement(252)), 
                m(intellimains.ChannelId.GEN_L2_L3_VOLTAGE, new UnsignedWordElement(253)), 
                m(intellimains.ChannelId.GEN_L3_L1_VOLTAGE, new UnsignedWordElement(254)), 
                m(intellimains.ChannelId.GEN_FREQUENCY, new UnsignedWordElement(255)),     
                new DummyRegisterElement(256),
                m(intellimains.ChannelId.GEN_L1_CURRENT, new UnsignedWordElement(257)),    
                m(intellimains.ChannelId.GEN_L2_CURRENT, new UnsignedWordElement(258)),    
                m(intellimains.ChannelId.GEN_L3_CURRENT, new UnsignedWordElement(259)),    
                m(intellimains.ChannelId.GEN_POWER_FACTOR, new SignedWordElement(260)),    
                new DummyRegisterElement(261, 262),
                m(intellimains.ChannelId.GEN_ACTIVE_POWER, new SignedWordElement(263)),    
                new DummyRegisterElement(264, 267),
                m(intellimains.ChannelId.GEN_REACTIVE_POWER, new SignedWordElement(268)),  
                new DummyRegisterElement(269, 272),
                m(intellimains.ChannelId.GEN_APPARENT_POWER, new SignedWordElement(273))   
            ),

            // Блок 4: обороты двигателя (40 315) — смещение 314
            new FC3ReadRegistersTask(314, Priority.HIGH,
                m(intellimains.ChannelId.ENGINE_SPEED, new UnsignedWordElement(314))       
            ),

            // Блок 5: статистика (43 587–43 598) — смещение 3 586–3 597
            new FC3ReadRegistersTask(3586, Priority.LOW,
                m(intellimains.ChannelId.ENGINE_RUN_HOURS, new SignedDoublewordElement(3586)), 
                m(intellimains.ChannelId.NUMBER_OF_STARTS, new UnsignedWordElement(3588)),     
                new DummyRegisterElement(3589, 3593),
                m(intellimains.ChannelId.TOTAL_KWH, new SignedDoublewordElement(3594)),        
                m(intellimains.ChannelId.TOTAL_KVARH, new SignedDoublewordElement(3596))       
            ),

            // Задача на запись — смещение 156
            new FC16WriteRegistersTask(156,
                this.controlCommandElement
            )
        );
    }

    @Override
    public String debugLog() {
        return "IntelliMains BB [" + this.id() + "]"
            + " | Режим: " + this.channel(intellimains.ChannelId.CONTROLLER_MODE).value().asString()
            + " | Состояние ДВС: " + this.channel(intellimains.ChannelId.ENGINE_STATE).value().asString()
            + " | RPM: " + this.channel(intellimains.ChannelId.ENGINE_SPEED).value().asString()
            + " | Мощность (kW): " + this.channel(intellimains.ChannelId.GEN_ACTIVE_POWER).value().asString()
            + " | L1-L2 (V): " + this.channel(intellimains.ChannelId.GEN_L1_L2_VOLTAGE).value().asString();
    }
}