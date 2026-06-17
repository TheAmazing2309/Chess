import java.util.*;
public class Chessboard{

	private static final int FROM_SHIFT   		= 0;
	private static final int TO_SHIFT     		= 6;
	private static final int MP_SHIFT     		= 12;
	private static final int CP_SHIFT     		= 16;
	private static final int PROMO_SHIFT  		= 20;
	private static final int NO_PIECE 			= 15;
	private static final int NO_EN_PASSANT 		= 64;

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
	private static final int WK_CASTLING_UNPACK_MASK = 1 << 0;
	private static final int WQ_CASTLING_UNPACK_MASK = 1 << 1;
	private static final int BK_CASTLING_UNPACK_MASK = 1 << 2;
	private static final int BQ_CASTLING_UNPACK_MASK = 1 << 3;

	private static final int INF = 100000000;
	private static final int MATE = 1000000;
	private static final int[] PIECE_VALUE = new int[]{100, 320, 330, 500, 900, 20000};

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
	public long[] bitboards = new long[12];
	public int[] pieceAt = new int[64];
	public int enPassantTile;
	public boolean whiteQueensideCastleRights, whiteKingsideCastleRights, blackQueensideCastleRights, blackKingsideCastleRights;

	public boolean turn;
	private int[] stateStack = new int[512];
	private int stackTop = 0;
	public int nodes = 0;

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
		updatePieceVarsFromBBs();
		updatePieceVarsToBBs();
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
	    updatePieceVarsFromBBs();
		updatePieceVarsToBBs();
	}

	public static int encodeMove(int from, int to, int movingPiece, int capturedPiece, int promotionPiece, int flags){
		/*[double pawn push 28][castling 27][en passant 26][promotion 25][capture 24][promotion piece 20-23][captured piece 16-19][moving piece 12-15][to 6-11][from 0-5]
		white pawn,knight,bishop,rook,queen,king 0-5; black 6-11*/
		return (from & 63) | ((to & 63) << TO_SHIFT) | ((movingPiece & 15) << MP_SHIFT) | ((capturedPiece & 15) << CP_SHIFT) | ((promotionPiece & 15) << PROMO_SHIFT) | flags;
	}
	public static int encodeState(int enPassantTile, int castleRights) {
    	return (((enPassantTile == -1) ? 64 : enPassantTile) & 0b1111111) | ((castleRights & 0b1111) << 7);
	}
	public int encodeCastlingRights(){
		return (whiteKingsideCastleRights ? WK_CASTLING_UNPACK_MASK : 0) | (whiteQueensideCastleRights ? WQ_CASTLING_UNPACK_MASK : 0) |
				(blackKingsideCastleRights ? BK_CASTLING_UNPACK_MASK : 0) | (blackQueensideCastleRights ? BQ_CASTLING_UNPACK_MASK : 0);
	}
	public static int decodeEnPassant(int state) {
	    return ((state & 0b1111111) == 64) ? -1 : (state & 0b1111111);
	}
	public static int decodeCastlingRights(int state) {
	    return (state >>> 7) & 0b1111;
	}
	public void updateCastlingRights(int newState){
		whiteKingsideCastleRights = whiteQueensideCastleRights = blackKingsideCastleRights = blackQueensideCastleRights = false;
		if ((newState & WK_CASTLING_UNPACK_MASK) != 0) whiteKingsideCastleRights = true;
		if ((newState & WQ_CASTLING_UNPACK_MASK) != 0) whiteQueensideCastleRights = true;
		if ((newState & BK_CASTLING_UNPACK_MASK) != 0) blackKingsideCastleRights = true;
		if ((newState & BQ_CASTLING_UNPACK_MASK) != 0) blackQueensideCastleRights = true;
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
	public ArrayList<Integer> pseudoLegalMoves(){
		ArrayList<Integer> out = pseudoLegalMovesPawn();
		out.addAll(pseudoLegalMovesKnight());
		out.addAll(pseudoLegalMovesSliding());
		out.addAll(pseudoLegalMovesKing());
		return out;
	}
	public ArrayList<Integer> legalMoves(){
		ArrayList<Integer> pseudo = pseudoLegalMoves();
		ArrayList<Integer> legal = new ArrayList<>();
		for (int move : pseudo){
			if ((move & FLAG_CASTLING) != 0) {
			    int kingFrom = move & 63;
			    int kingTo = (move >>> TO_SHIFT) & 63;
			    if (isSquareAttacked(kingFrom, !turn)) continue;
			    int step = (kingTo > kingFrom) ? 1 : -1;
			    int midSquare = kingFrom + step;
			    if (isSquareAttacked(midSquare, !turn)) continue;
			}
			makeMove(move);
			if (!isSquareAttacked(Long.numberOfTrailingZeros((!turn ? whiteKings : blackKings)), turn)) legal.add(move);
			undoMove(move);
		}
		return legal;
	}

	public void makeMove(int move){
		stateStack[stackTop++] = encodeState(enPassantTile, encodeCastlingRights());
		int from = move & 63;
		int to = (move >>> TO_SHIFT) & 63;
		int movingPiece = (move >>> MP_SHIFT) & 15;
		int capturedPiece = (move >>> CP_SHIFT) & 15;
		int promotionPiece = (move >>> PROMO_SHIFT) & 15;
		boolean isCapture = (move & FLAG_CAPTURE) != 0;
		boolean isPromotion = (move & FLAG_PROMOTION) != 0;
		boolean isEnPassant = (move & FLAG_EN_PASSANT) != 0;
		boolean isCastling = (move & FLAG_CASTLING) != 0;
		boolean isDoublePawnPush = (move & FLAG_DOUBLE_PUSH) != 0;
		long fromMask = 1L << from;
		long toMask = 1L << to;
		bitboards[movingPiece] ^= fromMask | toMask;
		pieceAt[from] = NO_PIECE;
		pieceAt[to] = movingPiece;
		if (isCapture && !isEnPassant) bitboards[capturedPiece] &= ~toMask;
		if (isCapture && isEnPassant){
			int capturedTile = to + (turn ? -8 : 8);
			bitboards[PAWN + (turn ? BLACK : WHITE)] &= ~(1L << capturedTile);
			pieceAt[capturedTile] = NO_PIECE;
		}
		if (isPromotion){
			bitboards[movingPiece] &= ~toMask;
			bitboards[promotionPiece] |= toMask;
			pieceAt[to] = promotionPiece;
		}
		if (isCastling) {
		    if (to == 6) {  
		        bitboards[ROOK + WHITE] ^= (1L << 7) | (1L << 5);
		        pieceAt[7] = NO_PIECE;
		        pieceAt[5] = ROOK + WHITE;
		    }
		    else if (to == 2) {
		        bitboards[ROOK + WHITE] ^= (1L << 0) | (1L << 3);
		        pieceAt[0] = NO_PIECE;
		        pieceAt[3] = ROOK + WHITE;
		    }
		    else if (to == 62) {
		        bitboards[ROOK + BLACK] ^= (1L << 63) | (1L << 61);
		        pieceAt[63] = NO_PIECE;
		        pieceAt[61] = ROOK + BLACK;
		    }
		    else if (to == 58) {
		        bitboards[ROOK + BLACK] ^= (1L << 56) | (1L << 59);
		        pieceAt[56] = NO_PIECE;
		        pieceAt[59] = ROOK + BLACK;
    		}
    		if (turn) whiteKingsideCastleRights = whiteQueensideCastleRights = false;
    		else blackKingsideCastleRights = blackQueensideCastleRights = false;
    	}
    	if (movingPiece == KING + WHITE) whiteKingsideCastleRights = whiteQueensideCastleRights = false;
    	if (movingPiece == KING + BLACK) blackKingsideCastleRights = blackQueensideCastleRights = false;
    	if (movingPiece == ROOK + WHITE || movingPiece == ROOK + BLACK){
    		if (from == 0 || to == 0) whiteQueensideCastleRights = false;
			if (from == 7 || to == 7) whiteKingsideCastleRights  = false;
			if (from == 56 || to == 56) blackQueensideCastleRights = false;
			if (from == 63 || to == 63) blackKingsideCastleRights  = false;
    	}
    	enPassantTile = -1;
    	if (isDoublePawnPush) enPassantTile = to + (turn ? -8 : 8);
    	updatePieceVarsToBBs();
    	turn = !turn;
	}
	public void undoMove(int move){
		int state = stateStack[--stackTop];
		enPassantTile = decodeEnPassant(state);
		updateCastlingRights(decodeCastlingRights(state));
		turn = !turn;
		int from = move & 63;
		int to = (move >>> TO_SHIFT) & 63;
		int movingPiece = (move >>> MP_SHIFT) & 15;
		int capturedPiece = (move >>> CP_SHIFT) & 15;
		int promotionPiece = (move >>> PROMO_SHIFT) & 15;
		boolean isCapture = (move & FLAG_CAPTURE) != 0;
		boolean isPromotion = (move & FLAG_PROMOTION) != 0;
		boolean isEnPassant = (move & FLAG_EN_PASSANT) != 0;
		boolean isCastling = (move & FLAG_CASTLING) != 0;
		boolean isDoublePawnPush = (move & FLAG_DOUBLE_PUSH) != 0;
		long fromMask = 1L << from;
		long toMask = 1L << to;
		if (isCastling){
			int rookFrom, rookTo;
			if (to > from){
				rookFrom = from + 3;
				rookTo = to - 1;
			} else {
				rookFrom = from - 4;
				rookTo = to + 1;
			}
			//System.out.println("Rook moved from " + rookFrom + " to " + rookTo);
			bitboards[ROOK + (turn ? WHITE : BLACK)] &= ~(1L << rookTo);
			bitboards[ROOK + (turn ? WHITE : BLACK)] |= (1L << rookFrom);
			pieceAt[rookTo] = NO_PIECE;
			pieceAt[rookFrom] = ROOK + (turn ? WHITE : BLACK);
		}
		pieceAt[to] = NO_PIECE;
		pieceAt[from] = movingPiece;
		if (isPromotion){
			bitboards[promotionPiece] &= ~toMask;
			bitboards[movingPiece] |= fromMask;
		}
		else{
			bitboards[movingPiece] ^= fromMask | toMask;
		}
		if (isCapture && !isEnPassant){
			bitboards[capturedPiece] |= toMask;
			pieceAt[to] = capturedPiece;
		}
		if (isCapture && isEnPassant){
			int capturedTile = to + (turn ? -8 : 8);
			bitboards[PAWN + (turn ? BLACK : WHITE)] |= (1L << capturedTile);
			pieceAt[to] = NO_PIECE;
			pieceAt[capturedTile] = PAWN + (turn ? BLACK : WHITE);
		}
		updatePieceVarsToBBs();
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
	private long genSlidingAttacksTurnless(int sq, int[] dirs) {
	    long attacks = 0L;
	    for (int dir : dirs) {
	        int s = sq;
	        while (true) {
	            s += dir;
	            if (s < 0 || s > 63 || Math.abs(file(s) - file(s - dir)) > 1) break;
	            long bb = 1L << s;
	            attacks |= bb;
	            if ((allPieces & bb) != 0) break;
	        }
	    }
	    return attacks;
	}

	public long perft(int depth){
		if (depth == 0) return 1;
		long nodes = 0L;
		ArrayList<Integer> moves = legalMoves();
		for (int move : moves){
			makeMove(move);
			nodes += perft(depth - 1);
			undoMove(move);
		}
		return nodes;
	}
	public int search(int depth, int alpha, int beta) {
		nodes++;
	    if (depth == 0)
	        return evaluate();

	    ArrayList<Integer> moves = legalMoves();
	    moves.sort((a, b) -> scoreMove(b) - scoreMove(a));
	    if (moves.size()==0){
	    	if (isSquareAttacked(Long.numberOfTrailingZeros(turn ? whiteKings : blackKings), !turn)){
	    		return turn ? -MATE : MATE;
	    	}
	    	else return 0;
	    }

	    if (turn) { // white (maximize)
	        for (int move : moves) {
	            makeMove(move);
	            int score = search(depth - 1, alpha, beta);
	            undoMove(move);

	            alpha = Math.max(alpha, score);
	            if (alpha >= beta)
	                break; // beta cutoff
	        }
	        return alpha;
	    } else { // black (minimize)
	        for (int move : moves) {
	            makeMove(move);
	            int score = search(depth - 1, alpha, beta);
	            undoMove(move);

	            beta = Math.min(beta, score);
	            if (beta <= alpha)
	                break; // alpha cutoff
	        }
	        return beta;
	    }
	}
	public int findBestMove(int depth) {
	    int bestMove = 0;
	    int bestScore = turn ? -INF : INF;

	    ArrayList<Integer> lm = legalMoves();
	    lm.sort((a, b) -> scoreMove(b) - scoreMove(a));

	    for (int move : lm) {
	        makeMove(move);
	        int score = search(depth - 1, -INF, INF);
	        undoMove(move);

	        if (turn && score > bestScore) {
	            bestScore = score;
	            bestMove = move;
	        }
	        if (!turn && score < bestScore) {
	            bestScore = score;
	            bestMove = move;
	        }
	    }
	    return bestMove;
	}


	public int evaluate(){
		int score = 0;
		score += PIECE_VALUE[PAWN] * Long.bitCount(whitePawns);
		score -= PIECE_VALUE[PAWN] * Long.bitCount(blackPawns);
		score += PIECE_VALUE[KNIGHT] * Long.bitCount(whiteKnights);
		score -= PIECE_VALUE[KNIGHT] * Long.bitCount(blackKnights);
		score += PIECE_VALUE[BISHOP] * Long.bitCount(whiteBishops);
		score -= PIECE_VALUE[BISHOP] * Long.bitCount(blackBishops);
		score += PIECE_VALUE[ROOK] * Long.bitCount(whiteRooks);
		score -= PIECE_VALUE[ROOK] * Long.bitCount(blackRooks);
		score += PIECE_VALUE[QUEEN] * Long.bitCount(whiteQueens);
		score -= PIECE_VALUE[QUEEN] * Long.bitCount(blackQueens);
		return score;
	}
	private int scoreMove(int move){
		int score = 0;
		boolean isCapture = (move & FLAG_CAPTURE) != 0;
		boolean isPromotion = (move & FLAG_PROMOTION) != 0;
		if (isPromotion) score += 1000000;
		if (isCapture) {
	        int victim = (move >>> CP_SHIFT) & 15;
	        int attacker = (move >>> MP_SHIFT) & 15;
	        score += 10 * PIECE_VALUE[victim % 6] - PIECE_VALUE[attacker % 6];
    	}
    	return score;
	}

	public boolean isSquareAttacked(int sq, boolean byWhite){
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
		if ((genSlidingAttacksTurnless(sq, bishopDir) & bishops) != 0) return true;
		long rooks = (byWhite ? whiteRooks | whiteQueens : blackRooks | blackQueens);
		if ((genSlidingAttacksTurnless(sq, rookDir) & rooks) != 0) return true;
		return false;
	}

	private void updatePieceVarsFromBBs(){
		whitePieces = whitePawns | whiteKnights | whiteBishops | whiteRooks | whiteQueens | whiteKings;
		blackPieces = blackPawns | blackKnights | blackBishops | blackRooks | blackQueens | blackKings;
		allPieces = whitePieces | blackPieces;
	    Arrays.fill(pieceAt, NO_PIECE);
	    bitboards[0] = fillFromBitboard(whitePawns,   PAWN   + WHITE);
	    bitboards[1] = fillFromBitboard(whiteKnights, KNIGHT + WHITE);
	    bitboards[2] = fillFromBitboard(whiteBishops, BISHOP + WHITE);
	    bitboards[3] = fillFromBitboard(whiteRooks,   ROOK   + WHITE);
	    bitboards[4] = fillFromBitboard(whiteQueens,  QUEEN  + WHITE);
	    bitboards[5] = fillFromBitboard(whiteKings,   KING   + WHITE);
	    bitboards[6] = fillFromBitboard(blackPawns,   PAWN   + BLACK);
	    bitboards[7] = fillFromBitboard(blackKnights, KNIGHT + BLACK);
	    bitboards[8] = fillFromBitboard(blackBishops, BISHOP + BLACK);
	    bitboards[9] = fillFromBitboard(blackRooks,   ROOK   + BLACK);
	    bitboards[10] = fillFromBitboard(blackQueens,  QUEEN  + BLACK);
	    bitboards[11] = fillFromBitboard(blackKings,   KING   + BLACK);
	}
	private void updatePieceVarsToBBs(){
		whitePawns   = bitboards[PAWN   + WHITE];
		whiteKnights = bitboards[KNIGHT + WHITE];
		whiteBishops = bitboards[BISHOP + WHITE];
		whiteRooks   = bitboards[ROOK   + WHITE];
		whiteQueens  = bitboards[QUEEN  + WHITE];
		whiteKings   = bitboards[KING   + WHITE];

		blackPawns   = bitboards[PAWN   + BLACK];
		blackKnights = bitboards[KNIGHT + BLACK];
		blackBishops = bitboards[BISHOP + BLACK];
		blackRooks   = bitboards[ROOK   + BLACK];
		blackQueens  = bitboards[QUEEN  + BLACK];
		blackKings   = bitboards[KING   + BLACK];

		whitePieces = whitePawns | whiteKnights | whiteBishops |
		              whiteRooks | whiteQueens  | whiteKings;

		blackPieces = blackPawns | blackKnights | blackBishops |
		              blackRooks | blackQueens  | blackKings;

		allPieces = whitePieces | blackPieces;
	}

	private long fillFromBitboard(long bb, int piece) {
		long copy = bb;
	    while (bb != 0) {
	        int sq = Long.numberOfTrailingZeros(bb);
	        pieceAt[sq] = piece;
	        bb &= bb - 1;
	    }
	    return copy;
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
	private static int rank(int x) { return (x >>> 3) + 1; }
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
		int from = move & 63;
		int to = (move >>> 6) & 63;
		if ((move & FLAG_CASTLING) != 0){
			return (from < to) ? "O-O" : "O-O-O";
		}
		String promo = ((move & FLAG_PROMOTION) != 0) ? "="+PIECE_NAMES_LETTERS[((move >>> 20) & 15) % 6] : "";
		String piece = PIECE_NAMES_LETTERS[((move >>> 12) & 15) % 6];
		String capture = ((move & FLAG_CAPTURE) != 0) ? "x" : "-";
		return piece + (char)(file(from) + 'a') + rank(from) + capture + (char)(file(to) + 'a') + rank(to) + promo;
	}

}