package edu.sjsu.cmpe272.simpleblog.server.zookeeper.util;

import org.springframework.beans.factory.annotation.Value;

public final class ZkDemoUtil {

  public static final String TEAM3 = "/team3";
  public static final String ALL_SERVERS = "/all_servers";
  public static final String LIVE_SERVERS = "/live_servers";
  public static final String LEADER = "/leader";
  public static final String PARTITIONS = "/partitions";
  public static final String PARTITION_LEADER = "/partition_leader";

  @Value("${server.port}")
  private static String serverPort;

  @Value("${server.ip}")
  private static String ip;

  private static String ipPort = null;

  public static String getHostPostOfServer() {
    if (ipPort != null) {
      return ipPort;
    }
    int port = Integer.parseInt(serverPort);
    ipPort = ip.concat(":").concat(String.valueOf(port));
    return ipPort;
  }

  public static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }

  private ZkDemoUtil() {}
}
