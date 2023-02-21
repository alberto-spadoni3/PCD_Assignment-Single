package part2.RMI.puzzleboard;

import part2.RMI.NodeHandlerSingleton;
import part2.actors.utility.StringEnum;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class PuzzleBoard extends JFrame {

    static final int ROWS1 = 3, ROWS2 = 4;
    static final int COLUMNS1 = 5, COLUMNS2 = 6;

    private final int rows, columns;
    private final String imagePath;
    private List<Tile> tiles = new ArrayList<>();
    private SelectionManager selectionManager = new SelectionManager();
    private final JPanel board;
    private BufferedImage image;


    public PuzzleBoard(final String title, StringEnum image) {
        imagePath = image.toString();
        boolean flagImg = imagePath.equals(StringEnum.IMAGE1.toString());
        rows = flagImg ? ROWS1 : ROWS2;
        columns = flagImg ? COLUMNS1 : COLUMNS2;

        setTitle(title);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);

    }

    public void createTiles() {

        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows * columns).forEach(randomPositions::add);
        Collections.shuffle(randomPositions);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                BufferedImage imagePortion = image.getSubimage(
                        (j * imageWidth / columns), // x
                        (i * imageHeight / rows),   // y
                        (imageWidth / columns),     // w
                        (imageHeight / rows));      // h

                tiles.add(new Tile(createSerializableImage(imagePortion), position, randomPositions.get(position)));
                position++;
            }
        }
    }

    public void paintPuzzle(final JPanel board) {
        board.removeAll();

        Collections.sort(tiles);

        tiles.forEach(tile -> {
            final TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> selectionManager.selectTile(tile,
                    () -> NodeHandlerSingleton.getInstance().handleSwap(this.tiles)));
        });
        checkSolution();
        pack();
    }

    private void checkSolution() {
        if (tiles.stream().allMatch(Tile::isInRightPlace)) {
            NodeHandlerSingleton.getInstance().communicateEndGame();
        }
    }

    // serialize image
    private byte[] createSerializableImage(final BufferedImage bufferedImage) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] byteImg = null;

        try {
            ImageIO.write(bufferedImage, "jpg", bos);
            bos.flush();
            byteImg = bos.toByteArray();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteImg;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(final List<Tile> updatedTiles) {
        tiles = updatedTiles;
        selectionManager = new SelectionManager();
    }

    public JPanel getPanel() {
        return board;
    }
}

