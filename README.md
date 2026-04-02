# StatsMod

A Fabric server-side mod for **Minecraft 1.21.1** that adds two informational commands.

## Commands

### `/stats`
Shows general server information:
- **Server Creation Date** — set manually with `/stats setcreation`
- **World File Size** — total size of the world directory in GB
- **In-Game Days** — number of Minecraft days elapsed since world creation

#### `/stats setcreation <date>` *(requires op level 2)*
Sets the server creation date shown by `/stats`.  
Example: `/stats setcreation 2024-06-15`

---

### `/joindate [<player>]`
Shows player-specific information (defaults to yourself):
- **First Join** — date/time the player first connected
- **Last Seen** — current session start (player must be online to query)
- **Time Played** — total accumulated play time from vanilla statistics

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) 0.104.0+1.21.1 or newer.
3. Drop `statsmod-1.0.0.jar` into your server's `mods/` folder.

## Building from Source

Requires Java 21 and Gradle 8.8 (or the Gradle Wrapper).

```bash
# Generate the Gradle wrapper (first time only, needs Gradle installed globally)
gradle wrapper

# Build the mod JAR
./gradlew build
```

The output JAR is located at `build/libs/statsmod-1.0.0.jar`.

## Data Storage

| File | Purpose |
|------|---------|
| `config/statsmod.json` | Server creation date (editable by ops via `/stats setcreation`) |
| `<world>/statsmod_data/<uuid>.json` | Per-player first join and last seen timestamps |

## License

MIT — see [LICENSE](LICENSE).
