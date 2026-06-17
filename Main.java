import java.util.*;
public class Main{

	public static String printBits(long x){ return String.format("%64s", Long.toBinaryString(x)).replace(' ', '0'); }
	public static String printBits(int x){ return String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0'); }
	public static long parseBits(String s){ return Long.parseUnsignedLong(s, 2); }
	
	public static void main(String[] args){
		Chessboard x;
		Scanner sc = new Scanner(System.in);
		if (args.length == 0) x = new Chessboard();
		else x = new Chessboard(String.join(" ", args));
		if (true) {
				/*
				ArrayList<Integer> movesP = x.pseudoLegalMovesPawn();
				ArrayList<Integer> movesN = x.pseudoLegalMovesKnight();
				ArrayList<Integer> movesS = x.pseudoLegalMovesSliding();
				ArrayList<Integer> movesK = x.pseudoLegalMovesKing();
				
				System.out.println(x);
				
				for (int i : movesP){
					System.out.println(Chessboard.moveToString(i));
				}
				System.out.println(movesP.size());
				for (int i : movesN){
					System.out.println(Chessboard.moveToString(i));
				}
				System.out.println(movesN.size());
				for (int i : movesS){
					System.out.println(Chessboard.moveToString(i));
				}
				System.out.println(movesS.size());
				for (int i : movesK){
					System.out.println(Chessboard.moveToString(i));
				}
				System.out.println(movesK.size());
				*/
				/*
		
				
				for (int i : moves){
					//int i = moves.get(moves.size()-1);
					String q = sc.nextLine();
					Text.clear();
					System.out.println(Chessboard.moveToString(i));
					System.out.println(x);
					x.makeMove(i);
					System.out.println(x);
					x.undoMove(i);
					System.out.println(x);
				//	break;
				}
				*/
		
				// 3qkq2/8/8/8/8/8/3n1n2/r2BKB1r w - - 0 1 bishops in
				// 3qkq2/8/8/8/8/8/2Bn1nB1/r3K2r w - - 0 1 bishops out
				/*
				System.out.println(x);
				ArrayList<Integer> movesFake = x.pseudoLegalMoves();
				ArrayList<Integer> moves = x.legalMoves();
				for (int i : moves) System.out.print(x.moveToString(i) + " ");
				System.out.println(" ");
				for (int i : movesFake) System.out.print(x.moveToString(i) + " ");
				*/
		
				/*
				ArrayList<Integer> stack = new ArrayList<>();
				int i =0;
				while(true){
					ArrayList<Integer> moves = x.legalMoves();
					if (moves.size() == 0){
						System.out.println("CHECKMATE OR STALEMATE");
						System.out.println(x);
						return;
					}
					int move = moves.get((int)(Math.random()*moves.size()));
					System.out.println("Move " + i + " " + x.moveToString(move));
					x.makeMove(move);
					stack.add(move);
					//if (i % 10 == 9) {System.out.println(x); String q = sc.nextLine();}
					i++;
				} 
				//System.out.println(x);
				/*
				String q = sc.nextLine();
				for (int j = 0; j < 100; j++) x.undoMove(stack.remove(stack.size()-1));
				System.out.println(x);
				
				*/
				/*
				ArrayList<Integer> movesFake = x.pseudoLegalMoves();
				for (int i : movesFake){
					String q = sc.nextLine();
					Text.clear();
					System.out.println(Chessboard.moveToString(i));
					//System.out.println(x);
					x.makeMove(i);
					System.out.println(x);
					//System.out.println(Long.numberOfTrailingZeros(x.whiteKings) + " " + x.isSquareAttacked(Long.numberOfTrailingZeros(x.whiteKings), false));
					for (int j = 0; j < 64; j++) System.out.println(j + " " + x.isSquareAttacked(j, x.turn));
					x.undoMove(i);
					System.out.println(x);
					
					//for (int j = 0; j < 64; j++) System.out.println(j + " " + x.isSquareAttacked(j, false));
				}
				*/
				
		
			//	System.out.println(printBits(x.blackRooks));
			//	System.out.println(printBits(x.blackKings));
			//	for (int j : x.pieceAt) System.out.print(j + " ");
				/*
				x.makeMove(movesS.get(9));
				System.out.println("Made move: " + Chessboard.moveToString(movesS.get(9)));
				System.out.println(x);
				x.undoMove(movesS.get(9));
				System.out.println("Undid move: " + Chessboard.moveToString(movesS.get(9)));
				System.out.println(x);
				*/}


		System.out.println(x);
		//int arr = x.legalMoves().get(0);
		//System.out.println("from " + (arr & 63));
		//System.out.println("to " + ((arr >>> 6) & 63));
		//System.out.println("mp " + ((arr >>> 12) & 15));
		for (int i = 0; i < 5; i++){
			int bestMove = x.findBestMove(6);
			System.out.println(x.moveToString(bestMove));
			x.makeMove(bestMove);
			//System.out.println("NODES: " + x.nodes);
		}
		
		//System.out.println("Legal: " + arr);
		//System.out.println("Best: " + bestMove);
/*
		for (int i = 0; i < 50; i++){
			x.nodes = 0;
			Text.clear();
			System.out.println(x);
			
			int bestMove = x.findBestMove(6);
			System.out.println(x.moveToString(bestMove));
			System.out.println("NODES: " + x.nodes);

			x.makeMove(bestMove);
			String q = sc.nextLine();
		}*/
		
	}
}