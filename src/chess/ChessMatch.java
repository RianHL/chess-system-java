package chess;

import boardGame.Board;
import boardGame.Piece;
import boardGame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {
    private int turn;
    private Color currentplayer;
    List<Piece> capturedPieces = new ArrayList<>();
    List<Piece> piecesOntheBoard = new ArrayList<>();
    private Board board;
    private boolean check;
    private boolean checkMate;
    
    public ChessMatch(){
        board = new Board(8, 8);
        turn = 1;
        currentplayer = Color.WHITE;
        initialSetup();
    }
    
    public int getTurn() {
        return turn;
    }

    public Color getCurrentplayer() {
        return currentplayer;
    }
    public boolean getCheck(){
        return check;
    }
    public boolean getCheckMate(){
        return checkMate;
    }
    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for(int i = 0; i < board.getRows(); i++){
            for(int j = 0; j < board.getRows(); j++){
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }
    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }
    public ChessPiece peformChessMovie(ChessPosition sourcePosition, ChessPosition targetPosition){
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);
        
        if(testCheck(currentplayer)){
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't pull yourself in check");
        }
        check = (testCheck(opponent(currentplayer))) ? true : false;
        if(testCheckMate(opponent(currentplayer))){
            checkMate = true;
        }
        else{
            nextTurn();
        }
        return (ChessPiece)capturedPiece;
    }
    private Piece makeMove(Position source, Position target){
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(p, target);
        if(capturedPiece != null){
            piecesOntheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }
    
    private void undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);
        if(capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOntheBoard.add(capturedPiece);
        }
    } 
    private void validateSourcePosition(Position position){
        if(!board.thereIsAPiece(position)){
            throw new ChessException("There is no piece on source position");
        }
        if (currentplayer != ((ChessPiece)board.piece(position)).getColor()){
            throw new ChessException("The chose piece is not yours");
        }
        if(!board.piece(position).isThereAnyPossibleMove()){
            throw new ChessException("There is no possible moves for the chosen piece");
        }
    }
    private void validateTargetPosition(Position source, Position target){
        if(!board.piece(source).possibleMove(target)){
            throw new ChessException("The chosen piece can't move to target position");
        }
    }
    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOntheBoard.add(piece);
    }
    private void nextTurn(){
        turn++;
        currentplayer = (currentplayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
    private Color opponent(Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
    private ChessPiece kingColor(Color color){
        List<Piece> list = piecesOntheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for(Piece p: list){
            if(p instanceof King){
                return (ChessPiece)p;
            }
                
        }
        throw new IllegalStateException("There is no " + color + "king of the board.");
    }
    private boolean testCheck(Color color){
        Position kingPosition = kingColor(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOntheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for(Piece p: opponentPieces){
            boolean[][] mat = p.possibleMoves();
            if(mat[kingPosition.getRow()][kingPosition.getColumn()]){
                return true;
            }
        }
        return false;
    }
    private boolean testCheckMate(Color color){
        if(!testCheck(color)){
            return false;
        }
        List<Piece> list = piecesOntheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for(Piece p: list){
            boolean[][] mat = p.possibleMoves();
            for(int i = 0; i < board.getRows(); i++){
                for(int j = 0; j < board.getRows(); j++){
                    if(mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if(!testCheck){
                            return false;
                        }
                    }
                }
        }
        }
        return true;
    }
    private void initialSetup(){
        placeNewPiece('a', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('b', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('c', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('d', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('e', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('f', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('g', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('h', 2, new Pawn(Color.WHITE, board));
        placeNewPiece('a', 1, new Rook(Color.WHITE, board));
        placeNewPiece('h', 1, new Rook(Color.WHITE, board));
        placeNewPiece('e', 1, new King(Color.WHITE, board));
        placeNewPiece('c', 1, new Bishop(Color.WHITE, board));
        placeNewPiece('f', 1, new Bishop(Color.WHITE, board));
        placeNewPiece('b', 1, new Knight(Color.WHITE, board));
        placeNewPiece('g', 1, new Knight(Color.WHITE, board));
        placeNewPiece('d', 1, new Queen(Color.WHITE, board));

        placeNewPiece('h', 8, new Rook(Color.BLACK, board));
        placeNewPiece('a', 8, new Rook(Color.BLACK, board));
        placeNewPiece('e', 8, new King(Color.BLACK, board));
        placeNewPiece('a', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('b', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('c', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('d', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('e', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('f', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('g', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('h', 7, new Pawn(Color.BLACK, board));
        placeNewPiece('c', 8, new Bishop(Color.BLACK, board));
        placeNewPiece('f', 8, new Bishop(Color.BLACK, board));
        placeNewPiece('b', 8, new Knight(Color.BLACK, board));
        placeNewPiece('g', 8, new Knight(Color.BLACK, board));
        placeNewPiece('d', 8, new Queen(Color.BLACK, board));
        
    }
}
