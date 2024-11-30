public class Block {
    boolean isDirty;
    boolean isEmpty;
    int tag;
    int recency;

    public Block() {
        isDirty = false;
        isEmpty = true;
        recency = 0;
    }
}
