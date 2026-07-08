package io.openems.edge.controller.deepsea8610mkii;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.bridge.modbus.api.ModbusComponent;

public interface deepsea8610mkii extends OpenemsComponent, ModbusComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        
        // --- УПРАВЛЕНИЕ РЕЖИМАМИ (Page 16) ---
        SET_CONTROL_MODE(Doc.of(OpenemsType.INTEGER)
            .accessMode(AccessMode.READ_WRITE)
            .text("Команда: 0=Stop, 1=Auto, 2=Manual, 3=Test")),

        // --- БАЗОВЫЕ СТАТУСЫ (Page 3) ---
        CONTROL_MODE(Doc.of(OpenemsType.INTEGER).text("Control Mode [10]")),
        OVERALL_STATUS(Doc.of(OpenemsType.INTEGER).text("Overall Status [11]")),

        // --- ДАТЧИКИ ДВИГАТЕЛЯ И АКБ (Page 4 и 5) ---
        OIL_PRESSURE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("kPa")),
        COOLANT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        OIL_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        FUEL_LEVEL(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        CHARGE_ALT_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        ENGINE_SPEED(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("RPM")),
        
        // --- ГЕНЕРАТОР: ЧАСТОТА, НАПРЯЖЕНИЯ, ТОКИ, АКТИВНАЯ МОЩНОСТЬ (Page 4 и 6) ---
        GEN_FREQUENCY(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIHERTZ)),
        GEN_L1_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L2_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L3_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L1_L2_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L2_L3_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L3_L1_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L1_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L2_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L3_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_EARTH_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L1_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_L2_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_L3_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_TOTAL_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),

        // --- АВТОМАТИЧЕСКИЙ РЕГУЛЯТОР НАПРЯЖЕНИЯ (AVR) ---
        AVR_OUTPUT_PERCENT(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT).text("AVR Output ")),
        AVR_OFFSET(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE).text("AVR IF Offset ")),

        // --- РЕАКТИВНАЯ МОЩНОСТЬ И КОСИНУС ФИ (Page 6) ---
        GEN_L1_VAR(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT_AMPERE_REACTIVE)),
        GEN_L2_VAR(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT_AMPERE_REACTIVE)),
        GEN_L3_VAR(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT_AMPERE_REACTIVE)),
        GEN_TOTAL_VAR(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT_AMPERE_REACTIVE)),
        GEN_PF_L1(Doc.of(OpenemsType.INTEGER).text("Cos Phi L1 ")),
        GEN_PF_L2(Doc.of(OpenemsType.INTEGER).text("Cos Phi L2 ")),
        GEN_PF_L3(Doc.of(OpenemsType.INTEGER).text("Cos Phi L3 ")),
        GEN_PF_TOTAL(Doc.of(OpenemsType.INTEGER).text("Cos Phi Total ")),

        // --- НАКОПЛЕННЫЕ СЧЕТЧИКИ (Page 7) ---
        ENGINE_RUN_TIME(Doc.of(OpenemsType.LONG).unit(Unit.SECONDS)),
        GEN_POSITIVE_KW_HOURS(Doc.of(OpenemsType.LONG).unit(Unit.WATT_HOURS)), 

        // --- ЦИФРОВЫЕ ВХОДЫ И ВЫХОДЫ (Сырые регистры для SCADA) ---
        DIGITAL_INPUTS_RAW(Doc.of(OpenemsType.INTEGER).text("Входы Digital A-L (Bits) ")),
        DIGITAL_OUTPUTS_RAW(Doc.of(OpenemsType.INTEGER).text("Выходы Digital A-L (Bits) ")),

        // --- УДАЛЕННОЕ УПРАВЛЕНИЕ  (Remote Control 1-9) ---
        REMOTE_CONTROL_1(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_2(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_3(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_4(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_5(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_6(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_7(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_8(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),
        REMOTE_CONTROL_9(Doc.of(OpenemsType.INTEGER).accessMode(AccessMode.READ_WRITE)),

        // --- АВАРИИ И СТАТУСЫ (33 Блока, Page 154) ---
        ALARM_TAG_1(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_2(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_3(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_4(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_5(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_6(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_7(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_8(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_9(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_10(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_11(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_12(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_13(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_14(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_15(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_16(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_17(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_18(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_19(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_20(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_21(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_22(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_23(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_24(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_25(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_26(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_27(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_28(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_29(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_30(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_31(Doc.of(OpenemsType.INTEGER)), ALARM_TAG_32(Doc.of(OpenemsType.INTEGER)),
        ALARM_TAG_33(Doc.of(OpenemsType.INTEGER));

        private final Doc doc;
        private ChannelId(Doc doc) { this.doc = doc; }
        @Override public Doc doc() { return this.doc; }
    }
}
