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
@Component(name = "DeepSea.8610mkIIv2", immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class deepsea8610Impl extends AbstractOpenemsModbusComponent implements deepsea8610mkii {

    private final Logger log = LoggerFactory.getLogger(deepsea8610Impl.class);

    // =========================================================================
    // БЕЗОПАСНЫЕ ФИЛЬТРЫ SENTINEL VALUES Использование Number для исключения ClassCastException
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

    // Физические элементы для отправки System Control Key
    private final UnsignedWordElement controlKeyElement = new UnsignedWordElement(4104);
    private final UnsignedWordElement controlKeyComplementElement = new UnsignedWordElement(4105);

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

        // Слушатель для отправки системных ключей управления (Страница 16: адреса 4104 и 4105)
        WriteChannel<Integer> controlModeChannel = this.channel(deepsea8610mkii.ChannelId.SET_CONTROL_MODE);
        controlModeChannel.onSetNextWrite(value -> {
            if (value != null) {
                int mode = ((Number) value).intValue();
                int key = 35700 + mode; 
                int complement = 65535 - key; 

                try {
                    this.controlKeyElement.setNextWriteValue(key);
                    this.controlKeyComplementElement.setNextWriteValue(complement);
                    this.logInfo(this.log, "Отправка команды управления DeepSea. Ключ: " + key);
                } catch (Exception e) {
                    this.logError(this.log, "Ошибка команды управления: " + e.getMessage());
                }
            }
        });
    }

    @Deactivate
    protected void deactivate() { super.deactivate(); }

    @Override
    protected ModbusProtocol defineModbusProtocol() {
        return new ModbusProtocol(this,
            
            // --- СТАТУС КОНТРОЛЛЕРА ---
            new FC3ReadRegistersTask(772, Priority.LOW,
                m(deepsea8610mkii.ChannelId.CONTROL_MODE, new UnsignedWordElement(772)),
                new DummyRegisterElement(773), 
                m(deepsea8610mkii.ChannelId.OVERALL_STATUS, new UnsignedWordElement(774))
            ),

            // --- БОЛЬШОЙ ПУЛ ДВИГАТЕЛЯ И ЭЛЕКТРИКИ (1024 - 1057) ---
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

            // --- AVR OUTPUT (Уровень) ---
            new FC3ReadRegistersTask(1220, Priority.LOW,
                m(deepsea8610mkii.ChannelId.AVR_OUTPUT_PERCENT, new UnsignedWordElement(1220), FILTER_U16)
            ),

            // --- ОБЩАЯ АКТИВНАЯ МОЩНОСТЬ ---
            new FC3ReadRegistersTask(1536, Priority.HIGH,
                m(deepsea8610mkii.ChannelId.GEN_TOTAL_WATTS, new SignedDoublewordElement(1536), FILTER_S32)
            ),

            // --- РЕАКТИВНАЯ МОЩНОСТЬ (VAr) И КОСИНУСЫ (Power Factor) (1546 - 1557) ---
            new FC3ReadRegistersTask(1546, Priority.HIGH,
                m(deepsea8610mkii.ChannelId.GEN_L1_VAR, new SignedDoublewordElement(1546), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_L2_VAR, new SignedDoublewordElement(1548), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_L3_VAR, new SignedDoublewordElement(1550), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_TOTAL_VAR, new SignedDoublewordElement(1552), FILTER_S32),
                m(deepsea8610mkii.ChannelId.GEN_PF_L1, new SignedWordElement(1554), FILTER_S16),
                m(deepsea8610mkii.ChannelId.GEN_PF_L2, new SignedWordElement(1555), FILTER_S16),
                m(deepsea8610mkii.ChannelId.GEN_PF_L3, new SignedWordElement(1556), FILTER_S16),
                m(deepsea8610mkii.ChannelId.GEN_PF_TOTAL, new SignedWordElement(1557), FILTER_S16)
            ),

            // --- НАКОПЛЕННЫЕ СЧЕТЧИКИ ТИПА LONG ---
            new FC3ReadRegistersTask(1798, Priority.LOW,
                m(deepsea8610mkii.ChannelId.ENGINE_RUN_TIME, new UnsignedDoublewordElement(1798), FILTER_U32_LONG),
                m(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS, new UnsignedDoublewordElement(1800), FILTER_U32_LONG_SCALE_100)
            ),

            // --- ЦИФРОВЫЕ ВХОДЫ (Сырой регистр 3089: Входы A-L) ---
            new FC3ReadRegistersTask(3089, Priority.LOW,
                m(deepsea8610mkii.ChannelId.DIGITAL_INPUTS_RAW, new UnsignedWordElement(3089))
            ),

            // --- ЦИФРОВЫЕ ВЫХОДЫ (Сырой регистр 3345: Выходы A-L) ---
            new FC3ReadRegistersTask(3345, Priority.LOW,
                m(deepsea8610mkii.ChannelId.DIGITAL_OUTPUTS_RAW, new UnsignedWordElement(3345))
            ),

            // --- AVR IF OFFSET ---
            new FC3ReadRegistersTask(35116, Priority.LOW,
                m(deepsea8610mkii.ChannelId.AVR_OFFSET, new SignedWordElement(35116))
            ),

            // --- АВАРИИ (33 Тэга, Страница 154) ---
            new FC3ReadRegistersTask(39425, Priority.LOW,
                m(deepsea8610mkii.ChannelId.ALARM_TAG_1, new UnsignedWordElement(39425)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_2, new UnsignedWordElement(39426)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_3, new UnsignedWordElement(39427)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_4, new UnsignedWordElement(39428)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_5, new UnsignedWordElement(39429)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_6, new UnsignedWordElement(39430)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_7, new UnsignedWordElement(39431)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_8, new UnsignedWordElement(39432)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_9, new UnsignedWordElement(39433)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_10, new UnsignedWordElement(39434)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_11, new UnsignedWordElement(39435)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_12, new UnsignedWordElement(39436)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_13, new UnsignedWordElement(39437)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_14, new UnsignedWordElement(39438)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_15, new UnsignedWordElement(39439)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_16, new UnsignedWordElement(39440)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_17, new UnsignedWordElement(39441)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_18, new UnsignedWordElement(39442)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_19, new UnsignedWordElement(39443)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_20, new UnsignedWordElement(39444)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_21, new UnsignedWordElement(39445)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_22, new UnsignedWordElement(39446)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_23, new UnsignedWordElement(39447)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_24, new UnsignedWordElement(39448)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_25, new UnsignedWordElement(39449)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_26, new UnsignedWordElement(39450)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_27, new UnsignedWordElement(39451)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_28, new UnsignedWordElement(39452)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_29, new UnsignedWordElement(39453)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_30, new UnsignedWordElement(39454)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_31, new UnsignedWordElement(39455)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_32, new UnsignedWordElement(39456)),
                m(deepsea8610mkii.ChannelId.ALARM_TAG_33, new UnsignedWordElement(39457))
            ),

            // --- ЗАПИСЬ РЕЖИМА УПРАВЛЕНИЯ (System Control Key) ---
            new FC16WriteRegistersTask(4104,
                this.controlKeyElement,
                this.controlKeyComplementElement
            ),

            // --- УДАЛЕННОЕ УПРАВЛЕНИЕ (Remote Control 1-9) ---
            new FC3ReadRegistersTask(49408, Priority.LOW,
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_1, new SignedWordElement(49408)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_2, new SignedWordElement(49409)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_3, new SignedWordElement(49410)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_4, new SignedWordElement(49411)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_5, new SignedWordElement(49412)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_6, new SignedWordElement(49413)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_7, new SignedWordElement(49414)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_8, new SignedWordElement(49415)),
                m(deepsea8610mkii.ChannelId.REMOTE_CONTROL_9, new SignedWordElement(49416))
            )
        );
    }

    // =========================================================================
    // ВЫВОД В КОНСОЛЬ (С ПРОВЕРКОЙ ФИЛЬТРОВ И SENTINEL ОШИБОК)
    // =========================================================================
    @Override
    public String debugLog() {
        String oilPress = this.channel(deepsea8610mkii.ChannelId.OIL_PRESSURE).value().isDefined()
            ? this.channel(deepsea8610mkii.ChannelId.OIL_PRESSURE).value().asString()
            : "ОБРЫВ/ОШИБКА";

        String energy = this.channel(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS).value().isDefined()
            ? this.channel(deepsea8610mkii.ChannelId.GEN_POSITIVE_KW_HOURS).value().asString()
            : "НЕТ ДАННЫХ";

        return "DSE 8610 [" + this.id() + "]"
            + " Режим: " + this.channel(deepsea8610mkii.ChannelId.CONTROL_MODE).value().asString()
            + " | Обороты: " + this.channel(deepsea8610mkii.ChannelId.ENGINE_SPEED).value().asString()
            + " | Масло кПа: " + oilPress
            + " | Мощность Вт: " + this.channel(deepsea8610mkii.ChannelId.GEN_TOTAL_WATTS).value().asString()
            + " | ВААр Total: " + this.channel(deepsea8610mkii.ChannelId.GEN_TOTAL_VAR).value().asString()
            + " | Выработка Wh: " + energy;
    }
}