package utm.ptm.mtransport.utils;

import java.time.LocalDateTime;

import utm.ptm.mtransport.data.models.Ticket;
import utm.ptm.mtransport.data.models.Transport;

public class TicketAM {
    public int id;
    public int transportId;
    public LocalDateTime creationTime;
    public LocalDateTime expirationTime;
    public String routeId;

    public TicketAM(Transport transport, Ticket ticket) {
        this.transportId = transport.getBoard();
        this.routeId = transport.getRouteId();
        this.creationTime = ticket.creationTime;
        this.id = ticket.id;
    }
}
