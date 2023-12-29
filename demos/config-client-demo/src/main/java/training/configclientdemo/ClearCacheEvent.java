package training.configclientdemo;

import lombok.NoArgsConstructor;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

@NoArgsConstructor
public class ClearCacheEvent extends RemoteApplicationEvent {

    public ClearCacheEvent(Object source, String originService, Destination destination) {
        super(source, originService, destination);
    }
}
