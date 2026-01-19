import java.util.*;
public class Main{

	public static String printBits(long x){ return String.format("%64s", Long.toBinaryString(x)).replace(' ', '0'); }
	public static String printBits(int x){ return String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0'); }
	public static long parseBits(String s){ return Long.parseUnsignedLong(s, 2); }
	
	public static void main(String[] args){
		Chessboard x;
		if (args.length == 0) x = new Chessboard();
		else x = new Chessboard(String.join(" ", args));
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
	}
}