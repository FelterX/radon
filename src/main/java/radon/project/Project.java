package radon.project;

import radon.engine.util.Version;

public class Project {
    private final String name;
    private final String path;
    private final Version version;
    private final GameType gameType;

    public Project(String name, String path, Version version, GameType gameType) {
        this.name = name;
        this.path = path;
        this.version = version;
        this.gameType = gameType;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Version getVersion() {
        return version;
    }

    public GameType getGameType() {
        return gameType;
    }
}
