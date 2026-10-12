# ProtoProx

**Compatible with Fabric (Minecraft 1.20.1+) and NeoForge (Minecraft 1.21.1+)**

**(Tested on Fabric 1.20.1, 1.21.1, 1.21.11, 26.3 and NeoForge 1.21.1, 1.21.11, 26.3)**

ProtoProx adds [Proxy Protocol](https://www.haproxy.org/download/3.2/doc/proxy-protocol.txt) (v1 and v2) support to Minecraft servers.

When a server sits behind a proxy or load balancer, such as HAProxy, nginx, TCPShield or playit.gg, every player appears to
connect from the proxy's IP address. ProtoProx reads the player's real address from the PROXY protocol header the proxy
sends, and applies it at the earliest possible point in the connection, before Minecraft even considers the connection
active.

The goal of this project is maximum IP compatibility: the server, its logs and every mod should only ever see the
player's real IP address, never the proxy's.

## What does it do?

* Supports Proxy Protocol v1 and v2, for both IPv4 and IPv6 players
* Applies the real IP before Minecraft activates the connection, so vanilla, Fabric, NeoForge and mods never see the proxy's IP
* The real IP shows up everywhere: logs, IP bans, whitelists, connection throttling, and mods that read the address from Minecraft or Netty
* One jar for both Fabric and NeoForge, across many Minecraft versions
* Server side only, with nothing to configure
* Rejects and logs connections with an invalid PROXY protocol header
* Leaves the connection untouched once the header is read, so there's no overhead while playing

## How do I set it up?

1. Put the jar in your server's `mods` folder
2. Turn on PROXY protocol (v1 or v2) in your proxy, for example `send-proxy-v2` in HAProxy or `proxy_protocol on;` in nginx
3. **Firewall your Minecraft server so only your proxy can reach it**

Every connection to the server is expected to start with a PROXY protocol header, and ProtoProx trusts the address in it.
If players or anyone else can reach the server directly, they can claim to be any IP address. Connections without a
header are dropped once Minecraft's connection timeout runs out.

## How do I obtain it?

You can download stable releases via Github Releases, [located here.](https://github.com/Puremin0rez/ProtoProx/releases)

You can download development builds via Github Actions, [located here.](https://github.com/Puremin0rez/ProtoProx/actions?query=branch%3Amain+is%3Asuccess) (Github Account Required)

You can compile it by running the following command in the project directory:

```
./gradlew clean build
```

(The jar file will be located in `/build/libs/`)

## Can I use your code?

This project is licensed as [MIT](LICENSE) and may be used in any way that respects the aforementioned license.

## Acknowledgements

* [HAProxy](https://www.haproxy.org) for creating the PROXY protocol.
* [Netty](https://netty.io) for its HAProxy codec, which ProtoProx uses to read the header.
* [Contributors](https://github.com/Puremin0rez/ProtoProx/graphs/contributors) for helping improve the project.
* You, for reading this and checking out the project.
