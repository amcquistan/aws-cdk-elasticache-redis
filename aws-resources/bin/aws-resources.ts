#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { NetworkStack } from '../lib/network-stack';
import { ElasticacheRedisStack } from '../lib/elasticache-redis-stack';
import { ProductsStack } from "../lib/products-stack";
import { FargateStack } from '../lib/fargate-stack';

import * as path from 'path';

const app = new cdk.App();

const env = { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION };

const networkStack = new NetworkStack(app, 'NetworkStack', {
  /* If you don't specify 'env', this stack will be environment-agnostic.
   * Account/Region-dependent features and context lookups will not work,
   * but a single synthesized template can be deployed anywhere. */

  /* Uncomment the next line to specialize this stack for the AWS Account
   * and Region that are implied by the current CLI configuration. */
  env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

  /* Uncomment the next line if you know exactly what Account and Region you
   * want to deploy the stack to. */
  // env: { account: '123456789012', region: 'us-east-1' },

  /* For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html */
});

const redisStack = new ElasticacheRedisStack(app, 'RedisStack', {
  env,
  vpc: networkStack.vpc,
});

const fargateStack = new FargateStack(app, 'FargateStack', {
  env,
  clusterName: 'redis-demo',
  vpc: networkStack.vpc
});

const productsStack = new ProductsStack(app, 'ProductsStack', {
  env,
  fargate: fargateStack.fargate,
  appPath: path.join(__dirname, '..', '..', 'products-service'),
  redisHost: 'master.rer1toxs3pbm4yhr.0gyrrw.use1.cache.amazonaws.com',
  redisPort: '6379',
  redisUsername: 'default',
  redisPassword: '1SuperDevelop3r!'
});
