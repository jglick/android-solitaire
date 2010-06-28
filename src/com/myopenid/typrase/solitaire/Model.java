package com.myopenid.typrase.solitaire;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
final class Model {
    public static final char[] RANKS = { // <editor-fold>
        ' ', //  no card
        'A',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        '0',
        'J',
        'Q',
        'K',
    }; // </editor-fold>
    public static final int RANK_SIZE = RANKS.length - 1;
    public static final int DEFAULT_SUIT_SIZE = 4;
    public static final int[] DEFAULT_ROWS = { // <editor-fold>
        0,
        0,
        3,
        4,
        5,
        6,
        7,
        6,
    };
    // ..34567..
    // .345676.
    // 345676.
    // 45676.
    // 5676.
    // 676.
    // 76.
    // ..
    // .           </editor-fold>
    private final byte[] matrix;
    private int maxMatrixX = 0;
    private int maxMatrixY = 0;
    private final byte[] deck;
    private int deckPointer;
    private final int deckSize;
    private byte flipped;
    public Model() {
        this(DEFAULT_ROWS, DEFAULT_SUIT_SIZE);
    }
    public Model(int[] rowSizes, int suitSize) {
        deckSize = RANK_SIZE * suitSize;
        matrix = new byte[deckSize * deckSize];
        List<Byte> _deck = new ArrayList<Byte>(deckSize);
        for (int i = 0; i < RANK_SIZE; i++) {
            for (int j = 0; j < suitSize; j++) {
                _deck.add((byte) (i + 1));
            }
        }
        Collections.shuffle(_deck);
        deck = new byte[deckSize];
        for (int i = 0; i < deckSize; i++) {
            deck[i] = _deck.get(i);
        }
        deckPointer = 0;
        for (int i = 0; i < rowSizes.length; i++) {
            int count = rowSizes[i];
            assert count <= i + 1;
            for (int j = 0; j < count; j++) {
                assert (i + 1 - count) % 2 == 0;
                int x = j + (i + 1 - count) / 2;
                int y = i - x;
                store(x, y, deal());
            }
        }
        flip();
    }
    /*
    public Model(byte[][] matrix, byte[] deck, byte flipped) {
        this.deck = deck;
        this.deckSize = 999; // XXX
        this.deckPointer = 0;
        // XXX set maxMatrix*
        // XXX set matrix
        this.matrix = null;
        assert false;
    }
     */
    public byte read(int x, int y) {
        if (x == deckSize || y == deckSize) {
            return 0; // simplify boundary logic
        }
        assert 0 <= x && x < deckSize && 0 <= y && y < deckSize;
        byte card = matrix[x * deckSize + y];
        assert card == 0 || (x <= maxMatrixX && y <= maxMatrixY);
        return card;
    }
    private void store(int x, int y, byte card) {
        assert 0 <= x && x < deckSize && 0 <= y && y < deckSize;
        assert 0 <= card && card <= RANK_SIZE;
        matrix[x * deckSize + y] = card;
        maxMatrixX = Math.max(maxMatrixX, x);
        maxMatrixY = Math.max(maxMatrixY, y);
    }
    private byte deal() {
        assert deckPointer < deck.length;
        return deck[deckPointer++];
    }
    public byte flipped() {
        return flipped;
    }
    public void flip() {
        flipped = deal();
    }
    public int deckRemaining() {
        return deck.length - deckPointer;
    }
    public int[] matrixSize() {
        int maxX = 0, maxY = 0;
        for (int x = 0; x <= maxMatrixX; x++) {
            for (int y = 0; y <= maxMatrixY; y++) {
                if (read(x, y) > 0) {
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return new int[] {maxX + 1, maxY + 1};
    }
    public int matrixBound() {
        return deckSize;
    }
    public int matrixRemaining() {
        int count = 0;
        for (int x = 0; x <= maxMatrixX; x++) {
            for (int y = 0; y <= maxMatrixY; y++) {
                if (read(x, y) > 0) {
                    count++;
                }
            }
        }
        return count;
    }
    public @Override String toString() {
        StringBuilder b = new StringBuilder("Model: ");
        b.append(RANKS[flipped]);
        int[] size = matrixSize();
        // XXX nicer to print in display layout
        for (int y = 0; y < size[1]; y++) {
            b.append('\n');
            for (int x = 0; x < size[0]; x++) {
                b.append(RANKS[read(x, y)]);
            }
        }
        return b.toString();
    }
    public static boolean adjacent(byte card1, byte card2) {
        assert 1 <= card1 && card1 <= RANK_SIZE;
        assert 1 <= card2 && card2 <= RANK_SIZE;
        return card1 + 1 == card2 ||
                card1 == card2 + 1 ||
                (card1 == 1 && card2 == RANK_SIZE) ||
                (card1 == RANK_SIZE && card2 == 1);
    }
    public boolean canAcquire(int x, int y) {
        byte c = read(x, y);
        if (c == 0 || !adjacent(flipped, c)) {
            return false;
        }
        if (read(x + 1, y) > 0 || read(x, y + 1) > 0) {
            return false;
        }
        return true;
    }
    public void acquire(int x, int y) {
        assert canAcquire(x, y);
        flipped = read(x, y);
        store(x, y, (byte) 0);
    }
}
