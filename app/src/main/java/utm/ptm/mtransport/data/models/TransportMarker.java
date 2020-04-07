package utm.ptm.mtransport.data.models;

import com.google.android.gms.maps.model.Marker;

import java.util.Collection;
import java.util.Objects;

public class TransportMarker {
    public Transport transport;
    public Marker marker;

    public TransportMarker(Transport transport) {
        this.transport = transport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransportMarker)) return false;
        TransportMarker that = (TransportMarker) o;
        return Objects.equals(transport, that.transport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transport);
    }
}
