import java.util.*;
import java.util.random.RandomGenerator;

public class NullableGraph {
    private final Set<Vertex> vertices = new HashSet<>();
    private final Set<NullableEdge> edges = new HashSet<>();
    private final List<NullableEdge> freeEdges = new ArrayList<>();
    private final RandomGenerator ng;

    public NullableGraph(RandomGenerator ng) {
        this.ng = ng;
    }

    public void add(Vertex v) {
        if (vertices.contains(v)) {
            throw new IllegalArgumentException("This vertex is already in the graph");
        }
        vertices.add(v);
        for (NullableEdge e : v.edges) {
            edges.add(e);
            if (e.hasNullEnd()) {
                freeEdges.add(e);
            }
        }
    }

    public NullableEdge mergeEdges(NullableEdge e1, NullableEdge e2) {
        if (edges.contains(e1) && edges.contains(e2)) {
            e1.merge(e2);
            edges.remove(e2);
            freeEdges.remove(e1);
            freeEdges.remove(e2);
        } else {
            throw new IllegalArgumentException("Both edges must belong to the graph");
        }
        return e1;
    }

    public NullableEdge findNullEdge(NullableEdge e1) {
        NullableEdge e = null;
        if (!hasFreeEdges()) {
            return null;
        }
        while (e == null || e1.shareEnd(e)) {
            e = findNullEdge();
        }
        return e;
    }

    public boolean hasFreeEdges() {
        return freeEdges.size() > 1;
    }

    public NullableEdge findNullEdge() {
        return freeEdges.get(ng.nextInt(0, freeEdges.size()));
    }

    static class Vertex {
        public Set<NullableEdge> edges = new HashSet<>();
        private NullableGraph graph;

        public Vertex(NullableGraph g) {
            this.graph = g;
        }

        public NullableEdge addEdge() {
            NullableEdge e = new NullableEdge(this);
            edges.add(e);
            graph.edges.add(e);
            graph.freeEdges.add(e);
            return e;
        }

        public NullableEdge addEdge(Vertex v) {
            NullableEdge e = new NullableEdge(this, v);
            edges.add(e);
            return e;
        }
    }

    static class NullableEdge {
        private final Vertex v1;
        private Vertex v2;

        public NullableEdge(Vertex v1) {
            this(v1, null);
        }
        public NullableEdge(Vertex v1, Vertex v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public void merge(NullableEdge e2) {
            if (hasNullEnd() && e2.hasNullEnd()) {
                v2 = e2.v1;
                v2.edges.remove(e2);
                v2.edges.add(this);
            } else {
                throw new IllegalStateException("Edge is already merged.");
            }
        }

        public boolean hasNullEnd() {
            return (v2 == null);
        }

        public boolean shareEnd(NullableEdge e) {
            return (v1 == e.v1 || v2 == e.v2 || v1 == e.v2 || v2 == e.v1);
        }

        public Vertex oppositeEnd(Vertex v) {
            assert v1 == v || v2 == v;
            if (v1 != v) {
                return v1;
            } else {
                return v2;
            }
        }
    }
}
