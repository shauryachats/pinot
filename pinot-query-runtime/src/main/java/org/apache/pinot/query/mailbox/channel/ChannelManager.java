/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.query.mailbox.channel;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pinot.common.config.TlsConfig;
import org.apache.pinot.common.utils.grpc.ServerGrpcQueryClient;
import org.apache.pinot.spi.utils.CommonConstants;


/**
 * {@code ChannelManager} manages Grpc send/receive channels.
 *
 * <p>Grpc channels are managed centralized per Pinot component. Channels should be reused across different
 * query/job/stages.
 */
public class ChannelManager {
  private final ConcurrentHashMap<Pair<String, Integer>, ManagedChannel> _channelMap = new ConcurrentHashMap<>();
  private final TlsConfig _tlsConfig;

  public ChannelManager(@Nullable TlsConfig tlsConfig) {
    _tlsConfig = tlsConfig;
  }

  public ManagedChannel getChannel(String hostname, int port) {
    // TODO: Revisit parameters
    if (_tlsConfig != null) {
      return _channelMap.computeIfAbsent(Pair.of(hostname, port),
          (k) -> NettyChannelBuilder
              .forAddress(k.getLeft(), k.getRight())
              .maxInboundMessageSize(
                  CommonConstants.MultiStageQueryRunner.DEFAULT_MAX_INBOUND_QUERY_DATA_BLOCK_SIZE_BYTES)
              .sslContext(ServerGrpcQueryClient.buildSslContext(_tlsConfig))
              .build()
      );
    } else {
      return _channelMap.computeIfAbsent(Pair.of(hostname, port),
          (k) -> ManagedChannelBuilder
              .forAddress(k.getLeft(), k.getRight())
              .maxInboundMessageSize(
                  CommonConstants.MultiStageQueryRunner.DEFAULT_MAX_INBOUND_QUERY_DATA_BLOCK_SIZE_BYTES)
              .usePlaintext()
              .build());
    }
  }
}
