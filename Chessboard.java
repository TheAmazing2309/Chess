import java.util.*;
public class Chessboard{

	private static final int FROM_SHIFT   		= 0;
	private static final int TO_SHIFT     		= 6;
	private static final int MP_SHIFT     		= 12;
	private static final int CP_SHIFT     		= 16;
	private static final int PROMO_SHIFT  		= 20;
	private static final int NO_PIECE 			= 15;

	private static final int FLAG_CAPTURE     	= 1 << 24;
	private static final int FLAG_PROMOTION   	= 1 << 25;
	private static final int FLAG_EN_PASSANT 	= 1 << 26;
	private static final int FLAG_CASTLING    	= 1 << 27;
	private static final int FLAG_DOUBLE_PUSH 	= 1 << 28;

	private static final long NO_A_FILE_MASK = 0xfefefefefefefefeL;
	private static final long NO_B_FILE_MASK = 0xfdfdfdfdfdfdfdfdL;
	private static final long NO_G_FILE_MASK = 0xbfbfbfbfbfbfbfbfL;
	private static final long NO_H_FILE_MASK = 0x7f7f7f7f7f7f7f7fL;
	private static final long NO_1_RANK_MASK = 0xffffffffffffff00L;
	private static final long NO_2_RANK_MASK = 0xffffffffffff00ffL;
	private static final long NO_7_RANK_MASK = 0xff00ffffffffffffL;
	private static final long NO_8_RANK_MASK = 0x00ffffffffffffffL;
	private static final long WHITE_QS_MASK = (1L << 1) | (1L << 2) | (1L << 3);
	private static final long BLACK_QS_MASK = (1L << 57) | (1L << 58) | (1L << 59);
	private static final long WHITE_KS_MASK = (1L << 5) | (1L << 6);
	private static final long BLACK_KS_MASK = (1L << 62) | (1L << 61);


	public static final int PAWN = 0;
	public static final int KNIGHT = 1;
	public static final int BISHOP = 2;
	public static final int ROOK = 3;
	public static final int QUEEN = 4;
	public static final int KING = 5;
	public static final int WHITE = 0;
	public static final int BLACK = 6;

	private static final int[] rookDir = new int[]{-1, 1, -8, 8};
	private static final int[] bishopDir = new int[]{-9, -7, 7, 9};
	private static final int[] queenDir = new int[]{-9, -8, -7, -1, 1, 7, 8, 9};

	public static final String[] PIECE_NAMES = new String[]{"Pawn", "Knight", "Bishop", "Rook", "Queen", "King"};
	public static final String[] PIECE_NAMES_LETTERS = new String[]{"", "N", "B", "R", "Q", "K"};
	public static final int[] PROMO_PIECES = new int[]{KNIGHT, BISHOP, ROOK, QUEEN};

	public long whitePawns;
	public long whiteKnights;
	public long whiteBishops;
	public long whiteRooks;
	public long whiteQueens;
	public long whiteKings;
	public long whitePieces;

	public long blackPawns;
	public long blackKnights;
	public long blackBishops;
	public long blackRooks;
	public long blackQueens;
	public long blackKings;
	public long blackPieces;

	public long allPieces;
	public int[] pieceAt = new int[64];
	public int enPassantTile;
	public boolean whiteQueensideCastleRights, whiteKingsideCastleRights, blackQueensideCastleRights, blackKingsideCastleRights;

	public boolean turn;

	private static long[] knightMoves = new long[64];
	static { for (int i = 0; i < 64; i++) knightMoves[i] = calculateKnightMoves(i); }
	private static long[] kingMoves = new long[64];
	static { for (int i = 0; i < 64; i++) kingMoves[i] = calculateKingMoves(i); }

	public Chessboard(){
		whitePawns = 65280L;
		whiteKnights = 66L;
		whiteBishops = 36L;
		whiteRooks = 129L;
		whiteQueens = 8L;
		whiteKings = 16L;

		blackPawns = 71776119061217280L;
		blackKnights = 4755801206503243776L;
		blackBishops = 2594073385365405696L;
		blackRooks = -9151314442816847872L;
		blackQueens = 576460752303423488L;
		blackKings = 1152921504606846976L;

		enPassantTile = -1;
		turn = true;
		whiteQueensideCastleRights = true;
		whiteKingsideCastleRights = true;
		blackQueensideCastleRights = true;
		blackKingsideCastleRights = true;
		updatePieceVars();
	}
	public Chessboard(String fen){
	    whitePawns = whiteKnights = whiteBishops = whiteRooks = whiteQueens = whiteKings = 0L;
	    blackPawns = blackKnights = blackBishops = blackRooks = blackQueens = blackKings = 0L;
	    enPassantTile = -1;
	    whiteQueensideCastleRights = false;
	    whiteKingsideCastleRights  = false;
	    blackQueensideCastleRights = false;
	    blackKingsideCastleRights  = false;
	    Arrays.fill(pieceAt, NO_PIECE);
	    String[] parts = fen.trim().split("\\s+");
	    if (parts.length < 4)
	        throw new IllegalArgumentException("Invalid FEN: " + fen);
	    String placement = parts[0];
	    String sideToMove = parts[1];
	    String castling   = parts[2];
	    String enPassant  = parts[3];
	    int sq = 56; // a8
	    for (int i = 0; i < placement.length(); i++){
	        char c = placement.charAt(i);
	        if (c == '/'){
	            sq -= 16;
	        }
	        else if (Character.isDigit(c)){
	            sq += c - '0';
	        }
	        else{
	            long bit = 1L << sq;
	            switch (c){
	                case 'P': whitePawns   |= bit; break;
	                case 'N': whiteKnights |= bit; break;
	                case 'B': whiteBishops |= bit; break;
	                case 'R': whiteRooks   |= bit; break;
	                case 'Q': whiteQueens  |= bit; break;
	                case 'K': whiteKings   |= bit; break;
	                case 'p': blackPawns   |= bit; break;
	                case 'n': blackKnights |= bit; break;
	                case 'b': blackBishops |= bit; break;
	                case 'r': blackRooks   |= bit; break;
	                case 'q': blackQueens  |= bit; break;
	                case 'k': blackKings   |= bit; break;
	                default:
	                    throw new IllegalArgumentException("Invalid FEN char: " + c);
	            }
	            sq++;
	        }
    	}
	    turn = sideToMove.equals("w");
	    if (!castling.equals("-")){
	        whiteKingsideCastleRights  = castling.indexOf('K') != -1;
	        whiteQueensideCastleRights = castling.indexOf('Q') != -1;
	        blackKingsideCastleRights  = castling.indexOf('k') != -1;
	        blackQueensideCastleRights = castling.indexOf('q') != -1;
	    }
	    if (!enPassant.equals("-")){
	        int file = enPassant.charAt(0) - 'a';
	        int rank = enPassant.charAt(1) - '1';
	        enPassantTile = rank * 8 + file;
	    }
	    updatePieceVars();
	}

	public static int encodeMove(int from, int to, int movingPiece, int capturedPiece, int promotionPiece, int flags){
		/*[double pawn push 28][castling 27][en passant 26][promotion 25][capture 24][promotion piece 20-23][captured piece 16-19][moving piece 12-15][to 6-11][from 0-5]
		white pawn,knight,bishop,rook,queen,king 0-5; black 6-11*/
		return (from & 63) | ((to & 63) << TO_SHIFT) | ((movingPiece & 15) << MP_SHIFT) | ((capturedPiece & 15) << CP_SHIFT) | ((promotionPiece & 15) << PROMO_SHIFT) | flags;
	}

	public ArrayList<Integer> pseudoLegalMovesPawn(){
		ArrayList<Integer> moves = new ArrayList<>();
		int dir = turn ? 8 : -8;
		int pieceColor = turn ? WHITE : BLACK; // THIS JUST SO HAPPENS TO BE PAWN CODE AS WELL
		long empty = ~allPieces;
		long oppColorPieces = turn ? blackPieces : whitePieces;

		long pawns = turn ? whitePawns : blackPawns;
		long rank2Pawns = turn ? whitePawns & (255L << 8) : blackPawns & (255L << 48);
		
		long oneTileMove = turn ? (pawns << 8) & empty : (pawns >>> 8) & empty;
		long twoTileMove = (turn ? ((rank2Pawns << 8) & empty) << 8 : ((rank2Pawns >>> 8) & empty) >>> 8) & empty;
		long attackTowardsA = turn ? (pawns & NO_A_FILE_MASK) << 7 : (pawns & NO_A_FILE_MASK) >>> 9;
		long attackTowardsH = turn ? (pawns & NO_H_FILE_MASK) << 9 : (pawns & NO_H_FILE_MASK) >>> 7;
		long captureTowardsA = attackTowardsA & oppColorPieces;
		long captureTowardsH = attackTowardsH & oppColorPieces;

		while (oneTileMove != 0){
			int to = Long.numberOfTrailingZeros(oneTileMove);
			int from = to - dir;
			if ((to >= 56 && turn) || (to <= 7 && !turn)){
				for (int i : PROMO_PIECES){
					moves.add(encodeMove(from, to, PAWN + pieceColor, NO_PIECE, i + pieceColor, FLAG_PROMOTION));
				}
			} 
			else moves.add(encodeMove(from, to, PAWN + pieceColor, NO_PIECE, NO_PIECE, 0));
			oneTileMove &= oneTileMove - 1;
		}
		while (twoTileMove != 0){
			int to = Long.numberOfTrailingZeros(twoTileMove);
			int from = to - dir * 2;
			moves.add(encodeMove(from, to, PAWN + pieceColor, NO_PIECE, NO_PIECE, FLAG_DOUBLE_PUSH));
			twoTileMove &= twoTileMove - 1;
		}
		while (captureTowardsA != 0){
			int to = Long.numberOfTrailingZeros(captureTowardsA);
			int from = to - (turn ? 7 : -9);
			if ((to >= 56 && turn) || (to <= 7 && !turn)){
				for (int i : PROMO_PIECES){
					moves.add(encodeMove(from, to, PAWN + pieceColor, pieceAt[to], i + pieceColor, FLAG_CAPTURE | FLAG_PROMOTION));
				}
			}
			else moves.add(encodeMove(from, to, PAWN + pieceColor, pieceAt[to], NO_PIECE, FLAG_CAPTURE));
			captureTowardsA &= captureTowardsA - 1;
		}
		while (captureTowardsH != 0){
			int to = Long.numberOfTrailingZeros(captureTowardsH);
			int from = to - (turn ? 9 : -7);
			if ((to >= 56 && turn) || (to <= 7 && !turn)){
				for (int i : PROMO_PIECES){
					moves.add(encodeMove(from, to, PAWN + pieceColor, pieceAt[to], i + pieceColor, FLAG_CAPTURE | FLAG_PROMOTION));
				}
			}
			else moves.add(encodeMove(from, to, PAWN + pieceColor, pieceAt[to], NO_PIECE, FLAG_CAPTURE));
			captureTowardsH &= captureTowardsH - 1;
		}
		if (enPassantTile != -1){
			long enPassantTowardsA = attackTowardsA & (1L << enPassantTile);
			long enPassantTowardsH = attackTowardsH & (1L << enPassantTile);
			if (enPassantTowardsA != 0){
				moves.add(encodeMove(enPassantTile - (turn ? 7 : -9), enPassantTile, PAWN + pieceColor, PAWN + (turn ? BLACK : WHITE), NO_PIECE, FLAG_CAPTURE | FLAG_EN_PASSANT));
			}
			if (enPassantTowardsH != 0){
				moves.add(encodeMove(enPassantTile - (turn ? 9 : -7), enPassantTile, PAWN + pieceColor, PAWN + (turn ? BLACK : WHITE), NO_PIECE, FLAG_CAPTURE | FLAG_EN_PASSANT));
			}
		}
		return moves;
	}
	public ArrayList<Integer> pseudoLegalMovesKnight(){
		ArrayList<Integer> moves = new ArrayList<>();
		long knights = turn ? whiteKnights : blackKnights;
		while (knights != 0){
			int from = Long.numberOfTrailingZeros(knights);
			long thisKnightMoves = knightMoves[from] & ~(turn ? whitePieces : blackPieces);
			while (thisKnightMoves != 0){
				int to = Long.numberOfTrailingZeros(thisKnightMoves);
				int pieceAtTo = pieceAt[to];
				int flag = (pieceAtTo == NO_PIECE) ? 0 : FLAG_CAPTURE;
				moves.add(encodeMove(from, to, KNIGHT + (turn ? WHITE : BLACK), pieceAtTo, NO_PIECE, flag));
				thisKnightMoves &= thisKnightMoves - 1;
			}
			knights &= knights - 1;
		}
		return moves;
	}
	public ArrayList<Integer> pseudoLegalMovesSliding(){
		ArrayList<Integer> out = new ArrayList<>();
		long bishops = turn ? whiteBishops : blackBishops;
		long rooks = turn ? whiteRooks : blackRooks;
		long queens = turn ? whiteQueens : blackQueens;
		while (bishops != 0){
			int from = Long.numberOfTrailingZeros(bishops);
			long moves = genSlidingMoves(from, bishopDir);
			while (moves != 0){
				int to = Long.numberOfTrailingZeros(moves);
				out.add(encodeMove(from, to, BISHOP + (turn ? WHITE : BLACK), pieceAt[to], NO_PIECE, (pieceAt[to] == NO_PIECE) ? 0 : FLAG_CAPTURE));
				moves &= moves - 1;
			}
			bishops &= bishops - 1;
		}
		while (rooks != 0){
			int from = Long.numberOfTrailingZeros(rooks);
			long moves = genSlidingMoves(from, rookDir);
			while (moves != 0){
				int to = Long.numberOfTrailingZeros(moves);
				out.add(encodeMove(from, to, ROOK + (turn ? WHITE : BLACK), pieceAt[to], NO_PIECE, (pieceAt[to] == NO_PIECE) ? 0 : FLAG_CAPTURE));
				moves &= moves - 1;
			}
			rooks &= rooks - 1;
		}
		while (queens != 0){
			int from = Long.numberOfTrailingZeros(queens);
			long moves = genSlidingMoves(from, queenDir);
			while (moves != 0){
				int to = Long.numberOfTrailingZeros(moves);
				out.add(encodeMove(from, to, QUEEN + (turn ? WHITE : BLACK), pieceAt[to], NO_PIECE, (pieceAt[to] == NO_PIECE) ? 0 : FLAG_CAPTURE));
				moves &= moves - 1;
			}
			queens &= queens - 1;
		}
		return out;
	}
	public ArrayList<Integer> pseudoLegalMovesKing(){
		ArrayList<Integer> moves = new ArrayList<>();
		long king = turn ? whiteKings : blackKings;
		int from = Long.numberOfTrailingZeros(king);
		long kingM = kingMoves[from] & ~(turn ? whitePieces : blackPieces);
		while (kingM != 0){
			int to = Long.numberOfTrailingZeros(kingM);
			int captured = pieceAt[to];
			int flag = (captured == NO_PIECE) ? 0 : FLAG_CAPTURE;
			moves.add(encodeMove(from, to, KING + (turn ? WHITE : BLACK), captured, NO_PIECE, flag));
			kingM &= kingM - 1;
		}
		if ((turn ? whiteQueensideCastleRights : blackQueensideCastleRights)){
			if ((allPieces & (turn ? WHITE_QS_MASK : BLACK_QS_MASK)) == 0) moves.add(encodeMove(from, from - 2, KING + (turn ? WHITE : BLACK), NO_PIECE, NO_PIECE, FLAG_CASTLING));
		}
		if ((turn ? whiteKingsideCastleRights : blackKingsideCastleRights)){
			if ((allPieces & (turn ? WHITE_KS_MASK : BLACK_KS_MASK)) == 0) moves.add(encodeMove(from, from + 2, KING + (turn ? WHITE : BLACK), NO_PIECE, NO_PIECE, FLAG_CASTLING));
		}
		return moves;
	}
	private long genSlidingMoves(int from, int[] dirs){
		long out = 0L;
		long oppColorPieces = turn ? blackPieces : whitePieces;
		long sameColorPieces = turn ? whitePieces : blackPieces;
		for (int dir : dirs){
			int start = from;
			while (true){
				start += dir;
				if (start < 0 || start > 63 || Math.abs(file(start) - file(start - dir)) > 1) break;
				long at = 1L << start;
				if ((at & sameColorPieces) != 0) break;
				out |= at;
				if ((at & oppColorPieces) != 0) break;
			}
		}
		return out;
	}

	private boolean isSquareAttacked(int sq, boolean byWhite){
		if (byWhite) {
		    if (((1L << sq) & ((whitePawns << 7) & NO_H_FILE_MASK)) != 0) return true;
		    if (((1L << sq) & ((whitePawns << 9) & NO_A_FILE_MASK)) != 0) return true;
		} else {
		    if (((1L << sq) & ((blackPawns >>> 7) & NO_A_FILE_MASK)) != 0) return true;
		    if (((1L << sq) & ((blackPawns >>> 9) & NO_H_FILE_MASK)) != 0) return true;
		}
		long knights = byWhite ? whiteKnights : blackKnights;
		if ((knightMoves[sq] & knights) != 0) return true;
		long king = byWhite ? whiteKings : blackKings;
		if ((kingMoves[sq] & king) != 0) return true;
		long bishops = (byWhite ? whiteBishops | whiteQueens : blackBishops | blackQueens);
		if ((genSlidingMoves(sq, bishopDir) & bishops) != 0) return true;
		long rooks = (byWhite ? whiteRooks | whiteQueens : blackRooks | blackQueens);
		if ((genSlidingMoves(sq, rookDir) & rooks) != 0) return true;
		return false;
	}

	private void updatePieceVars(){
		whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKings;
		blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKings;
		allPieces = whitePieces | blackPieces;
	    Arrays.fill(pieceAt, NO_PIECE);
	    fillFromBitboard(whitePawns,   PAWN   + WHITE);
	    fillFromBitboard(whiteKnights, KNIGHT + WHITE);
	    fillFromBitboard(whiteBishops, BISHOP + WHITE);
	    fillFromBitboard(whiteRooks,   ROOK   + WHITE);
	    fillFromBitboard(whiteQueens,  QUEEN  + WHITE);
	    fillFromBitboard(whiteKings,   KING   + WHITE);
	    fillFromBitboard(blackPawns,   PAWN   + BLACK);
	    fillFromBitboard(blackKnights, KNIGHT + BLACK);
	    fillFromBitboard(blackBishops, BISHOP + BLACK);
	    fillFromBitboard(blackRooks,   ROOK   + BLACK);
	    fillFromBitboard(blackQueens,  QUEEN  + BLACK);
	    fillFromBitboard(blackKings,   KING   + BLACK);
	}

	private void fillFromBitboard(long bb, int piece) {
	    while (bb != 0) {
	        int sq = Long.numberOfTrailingZeros(bb);
	        pieceAt[sq] = piece;
	        bb &= bb - 1;
	    }
	}
	private static long calculateKnightMoves(int sq){
		long knight = 1L << sq;
		long knightAttacks = 	((knight & NO_A_FILE_MASK) >>> 17) | ((knight & NO_H_FILE_MASK) >>> 15) |
								((knight & NO_A_FILE_MASK & NO_B_FILE_MASK) >>> 10) | ((knight & NO_H_FILE_MASK & NO_G_FILE_MASK) >>> 6) |
								((knight & NO_A_FILE_MASK & NO_B_FILE_MASK) << 6) | ((knight & NO_H_FILE_MASK & NO_G_FILE_MASK) << 10) |
								((knight & NO_A_FILE_MASK) << 15) | ((knight & NO_H_FILE_MASK) << 17);
		return knightAttacks;
	}
	private static long calculateKingMoves(int sq){
		long king = 1L << sq;
		long kingAttacks = 		((king & NO_A_FILE_MASK) >>> 1) | ((king & NO_A_FILE_MASK & NO_1_RANK_MASK) >>> 9) | 
								((king & NO_A_FILE_MASK & NO_8_RANK_MASK) << 7) | ((king & NO_8_RANK_MASK) << 8) | 
								((king & NO_1_RANK_MASK) >>> 8) | ((king & NO_8_RANK_MASK & NO_H_FILE_MASK) << 9) | 
								((king & NO_1_RANK_MASK & NO_H_FILE_MASK) >>> 7) | ((king & NO_H_FILE_MASK) << 1);
		return kingAttacks;
	}
	private static int file(int x) { return x & 7; }
	private static int rank(int x) { return x >>> 3; }
	private int getKingSquare(boolean color) { return Long.numberOfTrailingZeros((color ? whiteKings : blackKings)); }

	@Override public String toString(){
		String out = "";
		for (int rank = 7; rank >= 0; rank--){

			out += Text.colorize((rank + 1) + "  ", Text.GREEN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);

			for (int file = 0; file < 8; file++){

				long mask = 1L << rank * 8 + file;
				String add = Text.colorize(".", Text.CYAN+Text.BACKGROUND);

				if ((whitePawns & mask) != 0) add = Text.colorize("P", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((whiteKnights & mask) != 0) add = Text.colorize("N", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((whiteBishops & mask) != 0) add = Text.colorize("B", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((whiteRooks & mask) != 0) add = Text.colorize("R", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((whiteQueens & mask) != 0) add = Text.colorize("Q", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((whiteKings & mask) != 0) add = Text.colorize("K", Text.CYAN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT);
				else if ((blackPawns & mask) != 0) add = Text.colorize("P", Text.CYAN+Text.BACKGROUND, Text.BLACK);
				else if ((blackKnights & mask) != 0) add = Text.colorize("N", Text.CYAN+Text.BACKGROUND, Text.BLACK);
				else if ((blackBishops & mask) != 0) add = Text.colorize("B", Text.CYAN+Text.BACKGROUND, Text.BLACK);
				else if ((blackRooks & mask) != 0) add = Text.colorize("R", Text.CYAN+Text.BACKGROUND, Text.BLACK);
				else if ((blackQueens & mask) != 0) add = Text.colorize("Q", Text.CYAN+Text.BACKGROUND, Text.BLACK);
				else if ((blackKings & mask) != 0) add = Text.colorize("K", Text.CYAN+Text.BACKGROUND, Text.BLACK);

				out += add;
				if (file != 7) out += Text.colorize(" ", Text.CYAN+Text.BACKGROUND);

			}

			out += "\n";

		}
		out += Text.colorize(" ", Text.GREEN+Text.BACKGROUND);
		if (turn) out += Text.colorize("  ", Text.WHITE+Text.BACKGROUND);
		else out += Text.colorize("  ", Text.BLACK+Text.BACKGROUND);
		out += Text.colorize(" ".repeat(15), Text.GREEN+Text.BACKGROUND);
		return out + "\n" + Text.colorize("   a b c d e f g h", Text.GREEN+Text.BACKGROUND, Text.WHITE+Text.BRIGHT) + "\n";
	}
	public static String moveToString(int move){
		String out = "" + PIECE_NAMES_LETTERS[((move >>> 12) & 15) % 6];
		if ((move & FLAG_CAPTURE) != 0) out += "x";
		out += (char)(((move >>> 6) & 63) % 8 + 97);
		out += (((move >>> 6) & 63L) >>> 3) + 1;
		return out;
	}

}