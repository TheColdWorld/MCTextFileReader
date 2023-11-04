package cn.thecoldworld.textfilereader.networking;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class Events {
    public static Event<S2CEventArgs> S2CPackageEvent;
    public static Event<C2SEventArgs> C2SPackageEvent;

    public static class Event<ArgClass> {
        private final LinkedList<Consumer<ArgClass>> EventHandle;

        public Event() {
            EventHandle = new LinkedList<>();
        }

        public Event<ArgClass> Add(Consumer<ArgClass> dispatcher) {
            EventHandle.add(dispatcher);
            return this;
        }

        public void Invoke(ArgClass Args) {
            EventHandle.forEach(i -> i.accept(Args));
        }

        public void InvokeAsync(ArgClass Args) {
            EventHandle.forEach(i -> CompletableFuture.runAsync(() -> i.accept(Args)));
        }
    }
}
