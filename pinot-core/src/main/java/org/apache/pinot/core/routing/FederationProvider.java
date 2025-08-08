package org.apache.pinot.core.routing;

import java.util.Map;
import org.apache.pinot.common.config.provider.TableCache;


/**
 * A generic class which provides the dependencies for federation routing.
 */
public class FederationProvider {
  // Maps clusterName to TableCache.
  Map<String, TableCache> _tableCacheMap;

  public FederationProvider(Map<String, TableCache> tableCacheMap) {
    _tableCacheMap = tableCacheMap;
  }

  public Map<String, TableCache> getTableCacheMap() {
    return _tableCacheMap;
  }

  public TableCache getTableCache(String clusterName) {
    return _tableCacheMap.get(clusterName);
  }
}
