package radon.engine.resource;


import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.util.types.Singleton;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class ResourceManager extends RadonSystem {

    @Singleton
    private static ResourceManager instance;
    private final Set<Resource> resources;
    private boolean terminating;

    private ResourceManager(RadonSystemManager systemManager) {
        super(systemManager);
        resources = new HashSet<>();
    }

    public static boolean tracked(Resource resource) {
        return instance.resources.contains(resource);
    }

    public static synchronized void track(Resource resource) {
        if (!instance.terminating) {
            instance.resources.add(requireNonNull(resource));
        }
    }

    public static synchronized void untrack(Resource resource) {
        if (!instance.terminating) {
            instance.resources.remove(resource);
        }
    }

    @Override
    protected void init() {

    }

    @Override
    protected void terminate() {

        terminating = true;

        Iterator<Resource> iterator = resources.iterator();

        while (iterator.hasNext()) {

            final Resource resource = iterator.next();

            iterator.remove();

            if (resource instanceof ManagedResource) {
                ((ManagedResource) resource).releaseNoUntrack();
            } else {
                resource.release();
            }
        }
    }
}
