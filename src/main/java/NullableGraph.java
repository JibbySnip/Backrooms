import java.util.*;
import java.util.random.RandomGenerator;

public class NullableGraph {
    private final Set<Vertex> vertices = new HashSet<>();
    private final List<NullableEdge> edges = new LinkedList<>();
    private final List<NullableEdge> freeEdges = new LinkedList<>();
    private final RandomGenerator ng;

    public NullableGraph(RandomGenerator ng) {
        this.ng = ng;
    }

    private void add(Vertex v) {
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
        List<NullableEdge> nonEqualNullEdges = new ArrayList<>();
        for (NullableEdge e : freeEdges) {
            if (e1.v1 != e.v1) {
                nonEqualNullEdges.add(e);
            }
        }
        if (nonEqualNullEdges.size() == 0) {
            return null;
        } else {
            return pickRandom(nonEqualNullEdges);
        }
    }

    public NullableEdge findNullEdge() {
        return pickRandom(freeEdges);
    }

    private NullableEdge pickRandom(List<NullableEdge> es) {
        return es.get(ng.nextInt(0, es.size()));
    }

    public int freeEdgeCount() {
        return freeEdges.size();
    }

    static class Vertex {
        public Set<NullableEdge> edges = new HashSet<>();
        final NullableGraph graph;

        public Vertex(NullableGraph g) {
            this.graph = g;
            g.add(this);
        }

        public NullableEdge addEdge() {
            NullableEdge e = new NullableEdge(this);
            this.edges.add(e);
            graph.edges.add(e);
            graph.freeEdges.add(e);
            return e;
        }

        public NullableEdge addEdge(Vertex v) {
            NullableEdge e = new NullableEdge(this, v);
            edges.add(e);
            graph.edges.add(e);
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
