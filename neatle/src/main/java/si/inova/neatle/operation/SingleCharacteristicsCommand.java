package si.inova.neatle.operation;

import android.support.annotation.RestrictTo;

import java.util.UUID;

/**
 * Created by tomazs on 25. 05. 2017.
 */

abstract class SingleCharacteristicsCommand extends Command {

    protected final UUID serviceUUID;
    protected final UUID characteristicUUID;

    public SingleCharacteristicsCommand(UUID serviceUUID, UUID characteristicUUID, CommandObserver observer) {
        super(observer);
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    UUID getServiceUUID() {
        return serviceUUID;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    UUID getCharacteristicUUID() {
        return characteristicUUID;
    }
}
