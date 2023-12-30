import * as cdk from 'aws-cdk-lib';
import { Vpc, SecurityGroup, Peer, Port } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage } from "aws-cdk-lib/aws-ecs";
import { ApplicationLoadBalancedFargateService } from "aws-cdk-lib/aws-ecs-patterns";

import { Construct } from 'constructs';

import * as fs from 'fs';


export interface ProductsStackProps extends cdk.StackProps {
  readonly fargate: Cluster;
  readonly appPath: string;
  readonly redisHost: string;
  readonly redisPort: string;
  readonly redisUsername: string;
  readonly redisPassword: string;
}

export class ProductsStack extends cdk.Stack {

  constructor(scope: Construct, id: string, props: ProductsStackProps) {
    super(scope, id, props);
    
    if (!fs.existsSync(props.appPath)) {
      throw new Error(`invalid appPath ${props.appPath}`)
    }

    const fargateSvc = new ApplicationLoadBalancedFargateService(this, 'AppService', {
      assignPublicIp: true,
      cluster: props.fargate,
      taskImageOptions: {
        image: ContainerImage.fromAsset(props.appPath),
        containerPort: 8080,
        environment: {
          'SPRING_DATA_REDIS_HOST': props.redisHost,
          'SPRING_DATA_REDIS_PORT': props.redisPort,
          'SPRING_DATA_REDIS_USERNAME': props.redisUsername,
          'SPRING_DATA_REDIS_PASSWORD': props.redisPassword
        }
      },
    });

    fargateSvc.targetGroup.configureHealthCheck({
      path: '/actuator/health',
      port: '8080'
    });
  }
}
