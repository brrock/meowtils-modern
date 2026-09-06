---
tags:
  - Legit
---

# **PingHUD**

#### Displays your ping to the server.

!!! Note

    Due to how this module works, it may not be 100% accurate. It pings the server once per minute, which means temporary lag-spikes may not be detected. 
    
    It is recommended that you use the [LatencyAlerts](/modules/utility/latencyalerts/) module to detect lag-spikes instead.

---

## Dynamic color

Colors the ping text depending on how high it is.

**< 40ms =** <span style="color: #00aa00">Dark Green</span>

**< 100ms =** <span style="color: #55ff55">Green</span>

**< 150ms =** <span style="color: #ffff55">Yellow</span>

**< 200ms =** <span style="color: #ffaa00">Gold</span>

**< 300ms =** <span style="color: #ff5555">Red</span>

**> 300ms =** <span style="color: #aa0000">Dark Red</span>

_`< = Below`_

_`> = Above`_

## Text

Shows `Ping:` in front of the value.

## Brackets

Will wrap the text in brackets, for example `[Ping: 50ms]`.