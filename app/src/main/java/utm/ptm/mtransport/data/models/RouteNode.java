package utm.ptm.mtransport.data.models;

public class RouteNode {
    private long id;
    private Route route;

    public long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public Node getNode() {
        return node;
    }

    private Node node;
}
