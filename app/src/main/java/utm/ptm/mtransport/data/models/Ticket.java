package utm.ptm.mtransport.data.models;

import java.time.LocalDateTime;

public class Ticket {
    public int id;
    public int transportId;
    public String routeId;
    public LocalDateTime creationTime;
    public LocalDateTime expirationTime;
}
