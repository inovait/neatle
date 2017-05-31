package si.inova.neatle;

import si.inova.neatle.monitor.Connection;

/**
 * Created by tomazs on 30. 05. 2017.
 */

public interface ServicesDiscoveredListener {

    void onServicesDiscovered(Connection connection);
}
