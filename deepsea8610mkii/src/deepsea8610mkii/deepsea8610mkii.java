package deepsea8610mkii;

import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;


public interface deepsea8610mkii extends OpenemsComponent, ModbusComponent {

    // Объявляем наши 3 показателя для тестирования
    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
        CONTROL_MODE(Doc.of(OpenemsType.INTEGER)
            .text("0=Stop, 1=Auto, 2=Manual, 3=Test")), // Адрес 772
        
        BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER)
            .unit(Unit.MILLIVOLT)), 
        // Адрес 1029, масштаб 0.1 В
            
        ENGINE_SPEED(Doc.of(OpenemsType.INTEGER)
                .unit(Unit.NONE)
                .text("RPM")); 
        	// Адрес 1030       
    	
        private final Doc doc;
        private ChannelId(Doc doc) { this.doc = doc; }
        @Override public Doc doc() { return this.doc; }
    }
}