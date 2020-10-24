#!/usr/bin/env bash

# This script checks if an environment variable is set for the proxy. If so, maven is configured to use this proxy.
# This is a workaround as maven does not take the environment settings into account when downloading dependencies.

if [ ! -z ${https_proxy} ]; then
      echo 'generating maven config...';
      mkdir -p ~/.m2
      IFS=':' read -r address port <<< "${https_proxy:7}"
      echo "<settings>
              <proxies>
                <proxy>
                  <id>cnm-proxy</id>
                  <host>${address}</host>
                  <port>${port}</port>
                  <nonProxyHosts>artifacts.kpn.org</nonProxyHosts>
                </proxy>
              </proxies>
             </settings>" > ~/.m2/settings.xml ;
fi
