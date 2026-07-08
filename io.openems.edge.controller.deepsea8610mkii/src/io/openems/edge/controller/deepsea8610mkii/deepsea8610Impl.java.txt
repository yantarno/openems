package io.openems.edge.controller.deepsea8610mkii;

import org.osgi.service.cm.ConfigurationAdmin;
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
import io.openems.edge.common.channel.WriteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Designate(ocd = Config.class, factory = true)
@Component(name = "DeepSea.8610mkII", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)


public class deepsea8610Impl extends AbstractOpenemsModbusComponent implements deepsea8610mkii, OpenemsComponent, ModbusComponent {


    // =========================================================================
    // СИСТЕМА ФИЛЬТРАЦИИ SENTINEL VALUES ДЛЯ ЖИЗНЕННОГО ЦИКЛА 
    // =========================================================================
	private static final ElementToChannelConverter FILTER_U16 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).intValue() < 0xFFF8) ? ((Number) val).intValue() : null, val -> val);
	        
	    private static final ElementToChannelConverter FILTER_S16 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).shortValue() < 0x7FF8) ? ((Number) val).intValue() : null, val -> val);

	    private static final ElementToChannelConverter FILTER_U32 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).longValue() < 0xFFFFFFF8L) ? ((Number) val).intValue() : null, val -> val);
	        
	    private static final ElementToChannelConverter FILTER_S32 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).longValue() < 0x7FFFFFF8L) ? ((Number) val).intValue() : null, val -> val);

	    private static final ElementToChannelConverter FILTER_U16_SCALE_100 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).intValue() < 0xFFF8) ? ((Number) val).intValue() * 100 : null, val -> val);
	        
	    private static final ElementToChannelConverter FILTER_U32_SCALE_100 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).longValue() < 0xFFFFFFF8L) ? (int)(((Number) val).longValue() * 100) : null, val -> val);

	    private static final ElementToChannelConverter FILTER_U32_LONG = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).longValue() < 0xFFFFFFF8L) ? ((Number) val).longValue() : null, val -> val);
	        
	    private static final ElementToChannelConverter FILTER_U32_LONG_SCALE_100 = new ElementToChannelConverter(
	        val -> (val != null && ((Number) val).longValue() < 0xFFFFFFF8L) ? ((Number) val).longValue() * 100L : null, val -> val);

    // =========================================================================
    // ПЕРЕМЕННЫЕ ДЛЯ ЗАПИСИ (УПРАВЛЕНИЕ)
    // =========================================================================
    private final UnsignedWordElement controlKeyElement = new UnsignedWordElement(4104);
    private final UnsignedWordElement controlKeyComplementElement = new UnsignedWordElement(4105);

 // =========================================================================
    // ЛОГГЕР ДЛЯ ВЫВОДА СООБЩЕНИЙ В КОНСОЛЬ
    // =========================================================================
    private final Logger log = LoggerFactory.getLogger(deepsea8610Impl.class);

  
    
    @Reference
    protected ConfigurationAdmin cm;

    @Reference
    protected void setModbus(BridgeModbus modbus) { super.setModbus(modbus); }

    public deepsea8610Impl() {
        super(
            OpenemsComponent.ChannelId.values(),
            ModbusComponent.ChannelId.values(),
            deepsea8610mkii.ChannelId.values() 
        );
    }


    @Activate
    void activate(org.osgi.service.component.ComponentContext context, Config config) throws OpenemsException {
        if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus", config.modbus_id())) {
            return;
        }

        // 1. Явно получаем канал для записи (WriteChannel) типа Integer
        WriteChannel<Integer> controlModeChannel = this.channel(deepsea8610mkii.ChannelId.SET_CONTROL_MODE);
        
        // 2. Вешаем слушатель команд
        controlModeChannel.onSetNextWrite(value -> {
            if (value != null) {
            	int mode = ((Number) value).intValue();
                int key = 35700 + mode; // Вычисляем базовый ключ
                int complement = 65535 - key; // Вычисляем побитовое дополнение

                try {
                    this.controlKeyElement.setNextWriteValue(key);
                    this.controlKeyComplementElement.setNextWriteValue(complement);
                    this.logInfo(this.log, "Команда управления. Ключ: " + key);
                } catch (Exception e) {
                    this.logError(this.log, "Ошибка: " + e.getMessage());
                }
            }
        });
    }
    
    @Deactivate
    protected void deactivate() { super.deactivate(); }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
            
            // --- СТАТУС (Page 3) ---
            new FC3ReadRegistersTask(772, Priority.LOW,
                m(deepsea8610mkii.ChannelId.CONTROL_MODE, new UnsignedWordElement(772)),
                new DummyRegisterElement(773), 
                m(deepsea8610mkii.ChannelId.OVERALL_STATUS, new UnsignedWordElement(774))
            ),

            // --- БОЛЬШОЙ ПУЛ ДВИГАТЕЛЯ И ЭЛЕКТРИКИ (Page 4: 1024 - 1057) ---
            new FC3ReadRegistersTask(1024, Priority.HIGH,
                m(deepsea8610mkii.ChannelId.OIL_PRESSURE, new UnsignedWordElement(1024), FILTER_U16),
                m(deepsea8610mkii.ChannelId.COOLANT_TEMPERATURE, new SignedWordElement(1025), FILTER_S16),
                m(deepsea8610mkii.ChannelId.OIL_TEMPERATURE, new SignedWordElement(1026), FILTER_S16),
                m(deepsea8610mkii.ChannelId.FUEL_LEVEL, new UnsignedWordElement(1027), FILTER_U16),
                m(deepsea8610mkii.ChannelId.CHARGE_ALT_VOLTAGE, new UnsignedWordElement(1028), FILTER_U16_SCALE_100),
                m(deepsea8610mkii.ChannelId.BATTERY_VOLTAGE, new UnsignedWordElement(1029), FILTER_U16_SCALE_100),
                m(deepsea8610mkii.ChannelId.ENGINE_SPEED, new UnsignedWordElement(1030), FILTER_U16),
                m(deepsea8610mkii.ChannelId.GEN_FREQUENCY, new UnsignedWordElement(1031), FILTER_U16_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L1_N_VOLTAGE, new UnsignedDoublewordElement(1032), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L2_N_VOLTAGE, new UnsignedDoublewordElement(1034), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L3_N_VOLTAGE, new UnsignedDoublewordElement(1036), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L1_L2_VOLTAGE, new UnsignedDoublewordElement(1038), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L2_L3_VOLTAGE, new UnsignedDoublewordElement(1040), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L3_L1_VOLTAGE, new UnsignedDoublewordElement(1042), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L1_CURRENT, new UnsignedDoublewordElement(1044), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L2_CURRENT, new UnsignedDoublewordElement(1046), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L3_CURRENT, new UnsignedDoublewordElement(1048), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_EARTH_CURRENT, new UnsignedDoublewordElement(1050), FILTER_U32_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_L1_WATTS, new SignedDoublewordElement(1052), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_L2_WATTS, new SignedDoublewordElement(1054), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_L3_WATTS, new SignedDoublewordElement(1056), FILTER_S32)
            ),

            // --- РАСШИРЕННЫЕ ДАТЧИКИ (Page 5) ---
            new FC3ReadRegistersTask(1288, Priority.LOW,
                m(deepsea8610mkii.ChannelId.EXHAUST_TEMPERATURE, new SignedWordElement(1288), FILTER_S16),
                new DummyRegisterElement(1289),
                m(deepsea8610mkii.ChannelId.FUEL_CONSUMPTION, new UnsignedDoublewordElement(1290), FILTER_U32)
            ),
            new FC3ReadRegistersTask(1357, Priority.LOW,
                m(deepsea8610mkii.ChannelId.OIL_LEVEL_PERCENT, new UnsignedWordElement(1357), FILTER_U16),
                new DummyRegisterElement(1358),
                m(deepsea8610mkii.ChannelId.COOLANT_LEVEL_PERCENT, new UnsignedWordElement(1359), FILTER_U16)
            ),
            new FC3ReadRegistersTask(1397, Priority.LOW,
                m(deepsea8610mkii.ChannelId.TRIP_FUEL, new UnsignedDoublewordElement(1397), FILTER_U32)
            ),

            // --- ОБЩАЯ МОЩНОСТЬ (Page 6) ---
            new FC3ReadRegistersTask(1536, Priority.HIGH,
                m(deepsea8610mkii.ChannelId.GEN_TOTAL_WATTS, new SignedDoublewordElement(1536), FILTER_S32)
            ),

            // --- НАКОПЛЕННЫЕ СЧЕТЧИКИ ТИПА LONG (Page 7) ---
            new FC3ReadRegistersTask(1798, Priority.LOW,
                m(deepsea8610mkii.ChannelId.ENGINE_RUN_TIME, new UnsignedDoublewordElement(1798), FILTER_U32_LONG),
                m(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS, new UnsignedDoublewordElement(1800), FILTER_U32_LONG_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_NEGATIVE_KW_HOURS, new UnsignedDoublewordElement(1802), FILTER_U32_LONG_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_KVA_HOURS, new UnsignedDoublewordElement(1804), FILTER_U32_LONG_SCALE_100),
                m(deepsea8610mkii.ChannelId.GEN_KVAR_HOURS, new UnsignedDoublewordElement(1806), FILTER_U32_LONG_SCALE_100),
                m(deepsea8610mkii.ChannelId.NUMBER_OF_STARTS, new UnsignedDoublewordElement(1808), FILTER_U32_LONG)
            ),

            // --- НОМИНАЛЫ И ЛИМИТЫ (Page 137) ---
            new FC3ReadRegistersTask(35108, Priority.LOW,
                m(deepsea8610mkii.ChannelId.LOAD_LEVEL_MIN, new SignedWordElement(35108)),
                m(deepsea8610mkii.ChannelId.LOAD_LEVEL_MAX, new SignedWordElement(35109))
            ),
            new FC3ReadRegistersTask(35214, Priority.LOW,
                m(deepsea8610mkii.ChannelId.NOMINAL_VOLTAGE, new UnsignedDoublewordElement(35214), ElementToChannelConverter.SCALE_FACTOR_3)
            ),
            new FC3ReadRegistersTask(35217, Priority.LOW,
                m(deepsea8610mkii.ChannelId.NOMINAL_FREQUENCY, new UnsignedWordElement(35217), ElementToChannelConverter.SCALE_FACTOR_2)
            ),
            new FC3ReadRegistersTask(35221, Priority.LOW,
                m(deepsea8610mkii.ChannelId.CURRENT_LIMIT, new SignedWordElement(35221), ElementToChannelConverter.SCALE_FACTOR_3)
            ),

            // --- АЛЕРТЫ (Page 154) ---
            new FC3ReadRegistersTask(39425, Priority.LOW,
                m(deepsea8610mkii.ChannelId.ALARM_BLOCK_1, new UnsignedWordElement(39425)),
                m(deepsea8610mkii.ChannelId.ALARM_BLOCK_2, new UnsignedWordElement(39426))
            ),

            // --- ЗАПИСЬ РЕЖИМА УПРАВЛЕНИЯ (Page 16: Адреса 4104 и 4105) ---
            new FC16WriteRegistersTask(4104,
                this.controlKeyElement,
                this.controlKeyComplementElement
            )
        );
    }

    // =========================================================================
    // ВЫВОД В КОНСОЛЬ (С ПРОВЕРКОЙ ФИЛЬТРОВ И SENTINEL ОШИБОК)
    // =========================================================================
    @Override
    public String debugLog() {
        
        String voltL1L2 = this.channel(deepsea8610mkii.ChannelId.GEN_L1_L2_VOLTAGE).value().isDefined()
            ? this.channel(deepsea8610mkii.ChannelId.GEN_L1_L2_VOLTAGE).value().asString()
            : "НЕТ ДАННЫХ (Sentinel)";

        String oilPress = this.channel(deepsea8610mkii.ChannelId.OIL_PRESSURE).value().isDefined()
            ? this.channel(deepsea8610mkii.ChannelId.OIL_PRESSURE).value().asString()
            : "ОБРЫВ ДАТЧИКА";

        String energy = this.channel(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS).value().isDefined()
            ? this.channel(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS).value().asString()
            : "НЕТ ДАННЫХ";

        return "DSE 8610 [" + this.id() + "]"
            + " Режим: " + this.channel(deepsea8610mkii.ChannelId.CONTROL_MODE).value().asString()
            + " | Масло кПа: " + oilPress
            + " | Напр. L1-L2: " + voltL1L2
            + " | Обороты: " + this.channel(deepsea8610mkii.ChannelId.ENGINE_SPEED).value().asString()
            + " | Мощность: " + this.channel(deepsea8610mkii.ChannelId.GEN_TOTAL_WATTS).value().asString()
            + " | Выработка (Wh): " + energy;
    }
}
