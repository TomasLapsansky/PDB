package com.vut.fit.pdb2020.helper;

public class Pair<L, R> {

    private final L amount;
    private final R store;

    public Pair(L left, R store) {
        assert left != null;
        assert store != null;

        this.amount = left;
        this.store = store;
    }

    public L getAmount() { return amount; }
    public R getStore() { return store; }

    @Override
    public int hashCode() { return amount.hashCode() ^ store.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.amount.equals(pairo.getAmount()) &&
                this.store.equals(pairo.getStore());
    }
}
