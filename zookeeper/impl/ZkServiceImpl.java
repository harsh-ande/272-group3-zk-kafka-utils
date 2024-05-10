package edu.sjsu.cmpe272.simpleblog.server.zookeeper.impl;

import edu.sjsu.cmpe272.simpleblog.server.zookeeper.api.ZkService;
import edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.StringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.sjsu.cmpe272.simpleblog.server.zookeeper.util.ZkDemoUtil.*;

@Slf4j
public class ZkServiceImpl implements ZkService {

  private ZkClient zkClient;

  @Value("${server.partition.count}")
  private Integer partitionCount;

  
  public ZkServiceImpl(String hostPort) {
    zkClient = new ZkClient(hostPort, 12000, 3000, new StringSerializer());
  }

  public void closeConnection() {
    zkClient.close();
  }

  @Override
  public String getLeaderNodeData() {
    return zkClient.readData(LEADER, true);
  }

  @Override
  public Set<String> getAllPartitions() {
    Set<String> serverPartitions = new HashSet<>();
    if (!zkClient.exists(PARTITIONS)) {
      throw new RuntimeException("No node /partitions exists");
    }
    List<String> allPartitions = zkClient.getChildren(PARTITIONS);
    for(String partition: allPartitions) {
      List<String> partitionServers = zkClient.getChildren(partition);
      for (String server : partitionServers) {
        if (getHostPostOfServer().equals(zkClient.readData(server, true))) {
          serverPartitions.add(partition);
        }
      }
    }
    return serverPartitions;
  }

  @Override
  public String getPartitionLeader(Integer partition) {
    if (!zkClient.exists(PARTITION_LEADER)) {
      throw new RuntimeException("No node /partition_leader exists");
    }
    List<String> allPartitions = zkClient.getChildren(PARTITION_LEADER);
    for(String partitionNode: allPartitions) {
      if(partition.equals(Integer.parseInt(partitionNode))) {
        return zkClient.readData(partitionNode, true);
      }
    }
    return null;
  }
  @Override
  public void electForMaster() {
    if (!zkClient.exists(LEADER)) {
      zkClient.create(LEADER, "leader node", CreateMode.PERSISTENT);
    }
    try {
      zkClient.create(LEADER, getHostPostOfServer(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    } catch (ZkNodeExistsException e) {
      log.error("Leader already created!!, {0}", e);
    }
  }

  @Override
  public boolean leaderExists() {
    return zkClient.exists(LEADER);
  }

  @Override
  public void addToLiveNodes(String nodeName, String data) {
    if (!zkClient.exists(LIVE_SERVERS)) {
      zkClient.create(LIVE_SERVERS, "all live servers are displayed here", CreateMode.PERSISTENT);
    }
    String childNode = LIVE_SERVERS.concat("/").concat(nodeName);
    if (zkClient.exists(childNode)) {
      return;
    }
    zkClient.create(childNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
  }

  @Override
  public List<String> getLiveNodes() {
    if (!zkClient.exists(LIVE_SERVERS)) {
      throw new RuntimeException("No node /live_servers exists");
    }
    return zkClient.getChildren(LIVE_SERVERS);
  }

  @Override
  public void addToAllServers(String nodeName, String data) {
    if (!zkClient.exists(ALL_SERVERS)) {
      zkClient.create(ALL_SERVERS, "all live servers are displayed here", CreateMode.PERSISTENT);
    }
    String childNode = ALL_SERVERS.concat("/").concat(nodeName);
    if (zkClient.exists(childNode)) {
      return;
    }
    zkClient.create(childNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
  }

  @Override
  public List<String> getAllNodes() {
    if (!zkClient.exists(ALL_SERVERS)) {
      throw new RuntimeException("No node /all_servers exists");
    }
    return zkClient.getChildren(ALL_SERVERS);
  }

//  @Override
//  public List<String> getAllServers() {
//    if (!zkClient.exists(ALL_SERVERS)) {
//      throw new RuntimeException("No node /allNodes exists");
//    }
//    return zkClient.getChildren(ALL_SERVERS);
//  }

  @Override
  public void createAllParentNodes() {
    if (!zkClient.exists(TEAM3)) {
      zkClient.create(TEAM3, "node for team 3", CreateMode.PERSISTENT);
    }
    if (!zkClient.exists(ALL_SERVERS)) {
      zkClient.create(ALL_SERVERS, "all live nodes are displayed here", CreateMode.PERSISTENT);
    }
    if (!zkClient.exists(LIVE_SERVERS)) {
      zkClient.create(LIVE_SERVERS, "all live nodes are displayed here", CreateMode.PERSISTENT);
    }
    if (!zkClient.exists(LEADER)) {
      zkClient.create(LEADER, "Leader node", CreateMode.PERSISTENT);
    }
    if (!zkClient.exists(PARTITIONS)) {
      zkClient.create(PARTITIONS, "all partitions are displayed here", CreateMode.PERSISTENT);
//      createPartitions();
    }
    if (!zkClient.exists(PARTITION_LEADER)) {
      zkClient.create(PARTITION_LEADER, "partition leader is displayed here", CreateMode.PERSISTENT);
    }
  }

  @Override
  public void registerChildrenChangeWatcher(String path, IZkChildListener iZkChildListener) {
    zkClient.subscribeChildChanges(path, iZkChildListener);
  }

  @Override
  public void registerZkSessionStateListener(IZkStateListener iZkStateListener) {
    zkClient.subscribeStateChanges(iZkStateListener);
  }

  @Override
  public void createPartitions() {
    for (int i=0; i<partitionCount; i++) {
      String partitionPath = PARTITIONS.concat("/").concat(String.valueOf(i));
      if (!zkClient.exists(partitionPath)) {
        zkClient.create(partitionPath, "partition: "+i, CreateMode.PERSISTENT);
      }

    }
  }

  @Override
  public void addToAllPartitions(String host, String data) {
    if (!zkClient.exists(PARTITIONS)) {
      throw new RuntimeException("No node" + PARTITIONS + " exists");
    }
    List<String> partitions = zkClient.getChildren(PARTITIONS);
    for (String partition: partitions) {
      String partitionPath = PARTITIONS.concat("/").concat(String.valueOf(partition));
      String childNode = partitionPath.concat("/").concat(host);
      if (zkClient.exists(childNode)) {
        return;
      }
      zkClient.create(childNode, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }
  }

  @Override
  public void electForPartitionLeaders() {
    if (!zkClient.exists(PARTITION_LEADER)) {
      throw new RuntimeException("No node" + PARTITION_LEADER + " exists");
    }

    for (int i=0;i< partitionCount; i++) {
      String partitionLeaderPath = PARTITION_LEADER.concat("/").concat(String.valueOf(i));
      if (!zkClient.exists(partitionLeaderPath)) {
        zkClient.create(partitionLeaderPath, getHostPostOfServer(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
      }
    }

  }
}
