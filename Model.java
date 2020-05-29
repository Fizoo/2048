package com.javarush.task.task35.task3513;

import java.util.*;
import java.util.stream.Collectors;

public class Model {
    private final static int FIELD_WIDTH = 4;
    private Tile gameTiles[][] = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    int maxTile;
    long score;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Long> previousScores = new Stack<Long>();
    boolean isSaveNeeded = true;
    public static long x = 0;
    public static long y = 0;
    public static long z = 0;
    View view;

    public static long getX() {
        return x;
    }

    public Model() {
        resetGameTiles();

    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public void resetGameTiles() {
        Arrays.stream(gameTiles).forEach(a -> Arrays.setAll(a, i -> new Tile()));

        maxTile = 0;
        score = 0;

        addTile();
        addTile();

    }

    protected void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() != 0)
            emptyTiles.get((int) (Math.random() * emptyTiles.size())).value
                    = ((Math.random() < 0.9) ? 2 : 4);
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = Arrays.stream(gameTiles)
                .flatMap(Arrays::stream)
                .filter(Tile::isEmpty)
                .collect(Collectors.toList());
        return emptyTiles;

    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isComp = false;
        Tile[] tiles1 = Arrays.stream(tiles).toArray(Tile[]::new);
        Arrays.sort(tiles, Comparator.comparing(Tile::isEmpty));
        if (!Arrays.equals(tiles1, tiles))
            isComp = true;
        return isComp;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isMerge = false;

        for (int i = 1; i < tiles.length; i++) {
            if ((tiles[i - 1].value == tiles[i].value) && !tiles[i - 1].isEmpty() && !tiles[i].isEmpty()) {
                isMerge = true;
                tiles[i - 1].value *= 2;

                score += tiles[i - 1].value;
                maxTile = maxTile > tiles[i - 1].value ? maxTile : tiles[i - 1].value;
                tiles[i].value = 0;
                compressTiles(tiles);
            }
        }
        return isMerge;
    }

    public void left() {

        if (isSaveNeeded)
            saveState(gameTiles);
        int j = 0;
        for (Tile[] gameTile : gameTiles)
            if (compressTiles(gameTile) | mergeTiles(gameTile)) j++;

        if (j != 0) addTile();

        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    public void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }

    public void down() {
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }

    private void rotate() {
        Tile[][] newTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                newTile[j][FIELD_WIDTH - i - 1] = gameTiles[i][j];
            }
        }
        gameTiles = newTile;
    }

    public boolean canMove() {
        if (!getEmptyTiles().isEmpty())
            return true;
        for (Tile[] gameTile : gameTiles) {
            for (int j = 1; j < gameTiles.length; j++) {
                if (gameTile[j].value == gameTile[j - 1].value)
                    return true;
            }
        }

        for (int j = 0; j < gameTiles.length; j++) {
            for (int i = 1; i < gameTiles.length; i++) {
                if (gameTiles[i][j].value == gameTiles[i - 1][j].value)
                    return true;
            }

        }

        return false;

    }

    private void saveState(Tile[][] tiles) {
        Tile[][] newTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        Arrays.stream(newTile).forEach(a -> Arrays.setAll(a, i -> new Tile()));
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                newTile[i][j].value = tiles[i][j].value;
            }
        }
        int x = 0;
        previousStates.push(newTile);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (previousScores.isEmpty() | previousStates.isEmpty()) return;
        score = previousScores.pop();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j].value = previousStates.peek()[i][j].value;
            }
        }
        gameTiles = previousStates.pop();

    }

    public void randomMove() {

        int n = (int) ((Math.random() * 100) % 4);
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }

    }

    public boolean hasBoardChanged() {
        int sum1 = 0;
        int sum2 = 0;
        if (!previousStates.isEmpty()) {
            Tile[][] tiles = previousStates.peek();
            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    sum1 += gameTiles[i][j].value;
                    sum2 += tiles[i][j].value;
                }
            }
        }
        return sum1 != sum2;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        move.move();
        MoveEfficiency moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), (int) score, move);
        if (!hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return moveEfficiency;
    }

    public void autoMove() {

        ii();
        x++;
        //System.out.println(x);
    }

    public void autoMoveFinish() {
        while (canMove()) {
            ii();


        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println(x);
    }

    public void maxi() {
        Date date = new Date();
        boolean isLost = false;
        while (!isLost) {
            while (canMove()) {

                ii();
            }

            if (maxTile == 2048) {
                isLost = true;
            } else {
                z = z + score;
                resetGameTiles();
                y++;

            }

        }
        Date date1 = new Date();
        long res = (date1.getTime() - date.getTime()) / 1000;

        System.out.println(maxTile);
        System.out.println(score);
        System.out.println(y);
        System.out.println(z);
        System.out.println(res);
        System.out.println("------------------------------");
    }

    private void ii() {
        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.add(getMoveEfficiency(() -> left()));
        priorityQueue.add(getMoveEfficiency(() -> right()));
        priorityQueue.add(getMoveEfficiency(() -> up()));
        priorityQueue.add(getMoveEfficiency(() -> down()));

        priorityQueue.peek().getMove().move();
        x++;
        //System.out.println(score);

    }


}
