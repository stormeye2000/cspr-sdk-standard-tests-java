#!/usr/bin/env bash
BASEDIR=$(builtin cd ..; pwd)
# clear the assets folder
rm -rf  ${BASEDIR}/assets
mkdir ${BASEDIR}/assets
# copy net-1 users
docker cp storm-nctl:/home/casper/casper-node/utils/nctl/assets/net-1/users ${BASEDIR}/assets/net-1
# copy net-1 chainspec
docker cp storm-nctl:/home/casper/casper-node/utils/nctl/assets/net-1/chainspec ${BASEDIR}/assets/net-1
# copy faucet keys
docker cp storm-nctl:/home/casper/casper-node/utils/nctl/assets/net-1/faucet ${BASEDIR}/assets/net-1
