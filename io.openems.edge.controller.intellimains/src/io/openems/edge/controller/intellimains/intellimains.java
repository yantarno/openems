package io.openems.edge.controller.intellimains;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.bridge.modbus.api.ModbusComponent;

public interface intellimains extends OpenemsComponent, ModbusComponent {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        
        // --- УПРАВЛЕНИЕ И РЕЖИМЫ ---
        SET_CONTROL_MODE(Doc.of(OpenemsType.INTEGER)
            .accessMode(AccessMode.READ_WRITE)
            .text("Управление режимом")),
        CONTROLLER_MODE(Doc.of(OpenemsType.INTEGER).text("Текущий режим контроллера")),
        ENGINE_STATE(Doc.of(OpenemsType.INTEGER).text("Состояние двигателя")),

        // --- ДАТЧИКИ ДВИГАТЕЛЯ ---
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT).text("Напряжение АКБ (Ubat)")),
        OIL_PRESSURE(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("Давление масла (Bar)")),
        COOLANT_TEMPERATURE(Doc.of(OpenemsType.INTEGER).unit(Unit.DEGREE_CELSIUS).text("Температура охл. жидкости (T eng out)")),
        ENGINE_SPEED(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).text("Обороты (RPM)")),

        // --- ГЕНЕРАТОР: ЧАСТОТА И НАПРЯЖЕНИЯ L-N / L-L ---
        GEN_FREQUENCY(Doc.of(OpenemsType.INTEGER).unit(Unit.HERTZ)),
        GEN_L1_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GEN_L2_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GEN_L3_N_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GEN_L1_L2_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GEN_L2_L3_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),
        GEN_L3_L1_VOLTAGE(Doc.of(OpenemsType.INTEGER).unit(Unit.VOLT)),

        // --- ГЕНЕРАТОР: ТОКИ ФАЗ И МОЩНОСТЬ ---
        GEN_L1_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        GEN_L2_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        GEN_L3_CURRENT(Doc.of(OpenemsType.INTEGER).unit(Unit.AMPERE)),
        GEN_POWER_FACTOR(Doc.of(OpenemsType.INTEGER).text("Коэффициент мощности (Total)")),
        GEN_ACTIVE_POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOWATT)),
        GEN_REACTIVE_POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOVOLT_AMPERE_REACTIVE)),
        GEN_APPARENT_POWER(Doc.of(OpenemsType.INTEGER).unit(Unit.KILOVOLT_AMPERE)),

        // --- НАКОПЛЕННЫЕ СЧЕТЧИКИ ---
        ENGINE_RUN_HOURS(Doc.of(OpenemsType.LONG).text("Наработка часов")),
        NUMBER_OF_STARTS(Doc.of(OpenemsType.INTEGER).text("Количество запусков")),
        TOTAL_KWH(Doc.of(OpenemsType.LONG).unit(Unit.KILOWATT_HOURS)),
        TOTAL_KVARH(Doc.of(OpenemsType.LONG).unit(Unit.KILOVOLT_AMPERE_REACTIVE_HOURS));

        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }
}