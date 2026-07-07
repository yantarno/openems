package io.openems.edge.controller.deepsea8610mkii;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.bridge.modbus.api.ModbusComponent;

public interface deepsea8610mkii extends OpenemsComponent, ModbusComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        
        // --- УПРАВЛЕНИЕ КОНТРОЛЛЕРОМ (Page 16) ---
        SET_CONTROL_MODE(Doc.of(OpenemsType.INTEGER)
            .accessMode(AccessMode.READ_WRITE)
            .text("Команда управления: 0=Stop, 1=Auto, 2=Manual, 3=Test")),

        // --- СТАТУСЫ (Page 3) ---
        CONTROL_MODE(Doc.of(OpenemsType.INTEGER).text("Control Mode")),
        OVERALL_STATUS(Doc.of(OpenemsType.INTEGER).text("Overall Status")),

        // --- ДАТЧИКИ ДВИГАТЕЛЯ (Page 4 и 5) ---
        OIL_PRESSURE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("kPa")),
        COOLANT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        OIL_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        FUEL_LEVEL(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        CHARGE_ALT_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        ENGINE_SPEED(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("RPM")),
        
        EXHAUST_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS)),
        FUEL_CONSUMPTION(Doc.of(OpenemsType.INTEGER).text("Расход л/ч (x100)")), 
        OIL_LEVEL_PERCENT(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        COOLANT_LEVEL_PERCENT(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        TRIP_FUEL(Doc.of(OpenemsType.INTEGER).text("Топливо, Литры")),

        // --- ГЕНЕРАТОР: ЧАСТОТА И НАПРЯЖЕНИЯ L-N / L-L (Page 4) ---
        GEN_FREQUENCY(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIHERTZ)),
        GEN_L1_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L2_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L3_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L1_L2_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L2_L3_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        GEN_L3_L1_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        
        // --- ГЕНЕРАТОР: ТОКИ ФАЗ И МОЩНОСТЬ (Page 4 и 6) ---
        GEN_L1_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L2_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L3_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_EARTH_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),
        GEN_L1_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_L2_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_L3_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),
        GEN_TOTAL_WATTS(Doc.of(OpenemsType.INTEGER).unit(Unit.WATT)),

        // --- НАКОПЛЕННЫЕ СЧЕТЧИКИ (Page 7) ---
        ENGINE_RUN_TIME(Doc.of(OpenemsType.LONG).unit(Unit.SECONDS)),
        GEN_POSITIVE_KW_HOURS(Doc.of(OpenemsType.LONG).unit(Unit.WATT_HOURS)), 
        GEN_NEGATIVE_KW_HOURS(Doc.of(OpenemsType.LONG).unit(Unit.WATT_HOURS)),
        GEN_KVA_HOURS(Doc.of(OpenemsType.LONG).unit(Unit.VOLT_AMPERE_HOURS)),
        GEN_KVAR_HOURS(Doc.of(OpenemsType.LONG).unit(Unit.VOLT_AMPERE_REACTIVE_HOURS)),
        NUMBER_OF_STARTS(Doc.of(OpenemsType.LONG).unit(Unit.NONE).text("Запуски двигателя")),

        // --- БАЗОВЫЕ НОМИНАЛЫ (Page 137) ---
        LOAD_LEVEL_MIN(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        LOAD_LEVEL_MAX(Doc.of(OpenemsType.INTEGER).unit(Unit.PERCENT)),
        NOMINAL_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIVOLT)),
        NOMINAL_FREQUENCY(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIHERTZ)),
        CURRENT_LIMIT(Doc.of(OpenemsType.INTEGER).unit(Unit.MILLIAMPERE)),

        // --- АЛЕРТЫ (Page 154) ---
        ALARM_BLOCK_1(Doc.of(OpenemsType.INTEGER)),
        ALARM_BLOCK_2(Doc.of(OpenemsType.INTEGER));

        private final Doc doc;
        private ChannelId(Doc doc) { this.doc = doc; }
        @Override public Doc doc() { return this.doc; }
    }
}
