import * as cdk from 'aws-cdk-lib';
import { Vpc, SecurityGroup, Peer, Port } from "aws-cdk-lib/aws-ec2";
import { Cluster } from "aws-cdk-lib/aws-ecs";

import { Construct } from 'constructs';


export interface FargateStackProps extends cdk.StackProps {
  readonly vpc: Vpc;
  readonly clusterName: string;
}

export class FargateStack extends cdk.Stack {
  readonly fargate: Cluster;
  constructor(scope: Construct, id: string, props: FargateStackProps) {
    super(scope, id, props);
    
    this.fargate = new Cluster(this, 'FargateCluster', {
      vpc: props.vpc,
      clusterName: props.clusterName
    });

  }
}
