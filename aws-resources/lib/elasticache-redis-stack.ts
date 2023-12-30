import * as cdk from 'aws-cdk-lib';
import { Vpc, SecurityGroup, Peer, Port } from "aws-cdk-lib/aws-ec2";
import { CfnReplicationGroup, CfnSubnetGroup, CfnUser, CfnUserGroup } from 'aws-cdk-lib/aws-elasticache';

import { Construct } from 'constructs';


export interface ElasticacheRedisStackProps extends cdk.StackProps {
  readonly vpc: Vpc;
}

export class ElasticacheRedisStack extends cdk.Stack {
  /**
   *
   */
  constructor(scope: Construct, id: string, props: ElasticacheRedisStackProps) {
    super(scope, id, props);
    

    const subnetGroup = new CfnSubnetGroup(this, "RedisSubnetGrp", {
      description: "Secure subnet for isolating redis",
      subnetIds: props?.vpc.privateSubnets.map(sn => sn.subnetId),
      cacheSubnetGroupName: 'redis-subnet-group'
    });

    const securityGroup = new SecurityGroup(this, 'RedisSg', {
      vpc: props.vpc,
      allowAllOutbound: true
    });
    securityGroup.addIngressRule(
        Peer.ipv4(props.vpc.vpcCidrBlock),
        Port.allTraffic(),
        "Trust intra VPC traffic"
    );

    const defaultUser = new CfnUser(this, 'DefaultRedisUser', {
      engine: 'redis',
      userId: 'default-user',
      userName: 'default',
      accessString: 'on ~products-service:* -@all +@write +@read +@connection +info',
      authenticationMode: {
        "Type": "password"
      },
      passwords: ['1SuperDevelop3r!']
    });
    
    const userGrp = new CfnUserGroup(this, 'RedisUserGroup', {
      engine: 'redis',
      userGroupId: 'redis-group',
      userIds: [defaultUser.userId]
    });
    userGrp.addDependency(defaultUser)

    const redis = new CfnReplicationGroup(this, 'RedisReplicaGroup', {
      engine: "redis",
      cacheNodeType: "cache.t3.medium",
      automaticFailoverEnabled: true,
      engineVersion: "7.1",
      cacheParameterGroupName: 'default.redis7',
      numCacheClusters: 2,
      cacheSubnetGroupName: subnetGroup.cacheSubnetGroupName,
      replicationGroupDescription: "Redis Cluster using Cluster Mode Disabled with a Replica",
      atRestEncryptionEnabled: true,
      transitEncryptionEnabled: true,
      transitEncryptionMode: 'required',
      securityGroupIds: [securityGroup.securityGroupId],
      userGroupIds: [userGrp.userGroupId]
    });
    redis.addDependency(subnetGroup)
    redis.addDependency(userGrp)
  }
}


