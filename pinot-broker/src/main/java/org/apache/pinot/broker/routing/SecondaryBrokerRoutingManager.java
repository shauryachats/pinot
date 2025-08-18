package org.apache.pinot.broker.routing;

import java.util.HashMap;
import java.util.Map;
import org.apache.helix.AccessOption;
import org.apache.helix.HelixConstants;
import org.apache.helix.zookeeper.datamodel.ZNRecord;
import org.apache.pinot.common.metadata.ZKMetadataProvider;
import org.apache.pinot.common.metrics.BrokerMetrics;
import org.apache.pinot.common.utils.DatabaseUtils;
import org.apache.pinot.core.transport.server.routing.stats.ServerRoutingStatsManager;
import org.apache.pinot.spi.config.table.TableConfig;
import org.apache.pinot.spi.env.PinotConfiguration;
import org.apache.zookeeper.data.Stat;

import static org.apache.pinot.spi.utils.CommonConstants.Helix.BROKER_RESOURCE_INSTANCE;


public class SecondaryBrokerRoutingManager extends BrokerRoutingManager {

  Map<String, String> tableState;

  public SecondaryBrokerRoutingManager(BrokerMetrics brokerMetrics,
      ServerRoutingStatsManager serverRoutingStatsManager,
      PinotConfiguration pinotConfig) {
    super(brokerMetrics, serverRoutingStatsManager, pinotConfig);
    tableState = new HashMap<>();
  }

  @Override
  protected void processBrokerResourceConfigChange() {
    String brokerResourcePath = _externalViewPathPrefix + "brokerResource";
    Stat stat = new Stat();
    ZNRecord znRecord = _zkDataAccessor.get(brokerResourcePath, stat, AccessOption.PERSISTENT);

    if (znRecord == null || znRecord.getMapFields() == null) {
      return;
    }
    Map<String, Map<String, String>> zkTableState = znRecord.getMapFields();

    for (Map.Entry<String, Map<String, String>> entry : zkTableState.entrySet()) {
      String physicalOrLogicalTable = entry.getKey();
      Map<String, String> stateMap = entry.getValue();
      String currentTableState = tableState.getOrDefault(physicalOrLogicalTable, "OFFLINE");
      String newTableState = stateMap.values().stream().anyMatch("ONLINE"::equals) ? "ONLINE" : "OFFLINE";

      if ("ONLINE".equals(newTableState) && !"ONLINE".equals(currentTableState)) {
        // If the table state changes to ONLINE, we need to update the routing table
        System.out.println("BUILDING ROUTING FOR TABLE: " + physicalOrLogicalTable);
        if (ZKMetadataProvider.isLogicalTableExists(_propertyStore, physicalOrLogicalTable)) {
          buildRoutingForLogicalTable(physicalOrLogicalTable);
        } else {
          buildRouting(physicalOrLogicalTable);
        }
      } else if ("OFFLINE".equals(newTableState) && "ONLINE".equals(currentTableState)) {
        System.out.println("REMOVING ROUTING FOR TABLE: " + physicalOrLogicalTable);
        // If the table state changes to OFFLINE, we can remove it from the routing table
        if (ZKMetadataProvider.isLogicalTableExists(_propertyStore, physicalOrLogicalTable)) {
          removeRoutingForLogicalTable(physicalOrLogicalTable);
        } else {
          removeRouting(physicalOrLogicalTable);
        }
      }
      // Update the current state in the local map
      tableState.put(physicalOrLogicalTable, newTableState);
    }
  }

}
