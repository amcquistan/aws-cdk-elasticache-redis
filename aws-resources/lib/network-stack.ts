import * as cdk from "aws-cdk-lib";

import { Vpc, IpAddresses } from "aws-cdk-lib/aws-ec2";

import { Construct } from 'constructs';


export class NetworkStack extends cdk.Stack {
  readonly vpc: Vpc;
  // readonly securityGroup: SecurityGroup;
  /**
   *
   */
  constructor(scope: Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    
    this.vpc = new Vpc(this, 'Vpc', {
      ipAddresses: IpAddresses.cidr('10.0.0.0/16'),
      enableDnsHostnames: true,
      enableDnsSupport: true,
      maxAzs: 2,
      natGateways: 1
    });
  }
}


