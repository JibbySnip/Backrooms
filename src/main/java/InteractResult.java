public record InteractResult(int shekelsGained, NullableGraph.NullableEdge exitTaken,
                             NullableGraph.Vertex originRoom, boolean gameOver) {
}
