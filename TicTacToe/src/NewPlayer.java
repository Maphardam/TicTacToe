import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IMove;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

import java.io.ObjectInputStream.GetField;
import java.util.Arrays;

/**
 * Some comments ...
 * 
 * @author Tilman Krokotsch, 199917
 */
public class NewPlayer implements IPlayer {

	private double[] weights = {0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1};
	private boolean learning = true;

	public String getName() {
		// TODO Auto-generated method stub
		return "test";
	}

	public int[] makeMove(IBoard board) {
		// TODO Auto-generated method stub

		// create a clone of the board that can be modified
		IBoard copy = board.clone();

		int[] movePos = decideBestMove(copy);
		System.out.println(Arrays.toString(movePos));
		// do a move using the cloned board
		try {
			copy.makeMove(new Move(this, movePos));
		} catch (IllegalMoveException e) {
			// move was not allowed
		}

		// return your final decision for your next move
		return movePos;
	}

	public void onMatchEnds(IBoard board) {
		if (learning) {

		} else {

		}
		return;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// ***********************************OWN**FUNCTIONS***************************************//
	// //////////////////////////////////////////////////////////////////////////////////////////

	private int[] decideBestMove(IBoard board) {
		int dim = board.getSize();
		double maxValue = -100;
		int[] bestMove = null;

		for (int x = 0; x < dim; x++) {
			for (int y = 0; y < dim; y++) {
				for (int z = 0; z < dim; z++) {

					if (board.getFieldValue(new int[] { x, y, z }) != null)
						break;

					double value = computeValueAtPosition(board, x, y, z);
					if (value > maxValue) {
						maxValue = value;
						bestMove = new int[] { x, y, z };
					}

				}
			}
		}

		return bestMove;
	}


	private double computeValueAtPosition (IBoard _board, int x, int y, int z) {
		
		IBoard board = _board.clone();
		
		int[] currentFeatures = getFeatures(board);
		
		double currentSum = 0;
		for (int i = 0; i < 10; i++) {
			currentSum += currentFeatures[i] * weights[i];
		}

		try {
			board.makeMove(new Move(this, new int[] {x,y,z}));
		} catch(IllegalMoveException e) {}
		
		int[] futureFeatures = getFeatures(board);
		
		double futureSum = 0;
		for (int i = 0; i < 10; i++) {
			futureSum += futureFeatures[i] * weights[i];
		}
		
		if (x == 2 && y == 2 && z == 2) System.out.println();
		return futureSum - currentSum;		
	}

	private int[] getFeatures(IBoard board) {
		int[] xLines = checkX(board);
		int[] yLines = checkY(board);
		int[] zLines = checkZ(board);
		int[] diagXLines = checkDiagX(board);
		int[] diagYLines = checkDiagY(board);
		int[] diagZLines = checkDiagZ(board);
		int[] diag3DLines = check3D(board);

		int[] features = new int[10];
		
		for (int i = 0; i < 10; i++) {
			features[i] += xLines[i] + yLines[i] + zLines[i] + diagXLines[i]
					+ diagYLines[i] + diagZLines[i] + diag3DLines[i];
		}
		return features;
	}

	private int[] checkX(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();
		// Traverse board in horizontal x direction
		for (int z = 0; z < dim; z++) {
			for (int y = 0; y < dim; y++) {
				IPlayer player = null;
				int count = 0;

				for (int x = 0; x < dim; x++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });
					// When it is the first field the row the player to count is
					// set.
					// It does not matter which player is counted, as the row is
					// not valid i two players occupy fields there.
					if (occupier != null) {
						if (player == null) {
							player = occupier;
							count++;
						} else {
							if (player != occupier) {
								count = 0;
								break;
							} else {
								count++;
							}
						}
					}

				}

				if (count != 0) {
					if (player == this)
						result[count - 1]++;
					else
						result[count + 4]++;
				}
			}
		}
		return result;
	}

	private int[] checkY(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();

		// Traverse board in horizontal y direction
		for (int x = 0; x < dim; x++) {
			for (int z = 0; z < dim; z++) {
				IPlayer player = null;
				int count = 0;

				for (int y = 0; y < dim; y++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });
					// When it is the first field the row the player to count is
					// set.
					// It does not matter which player is counted, as the row is
					// not valid i two players occupy fields there.
					if (occupier != null) {
						if (player == null) {
							player = occupier;
							count++;
						} else {
							if (player != occupier) {
								count = 0;
								break;
							} else {
								count++;
							}
						}
					}

				}

				if (count != 0) {
					if (player == this)
						result[count - 1]++;
					else
						result[count + 4]++;
				}
			}
		}
		return result;
	}

	private int[] checkZ(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();

		// Traverse board in horizontal z direction
		for (int x = 0; x < dim; x++) {
			for (int y = 0; y < dim; y++) {
				IPlayer player = null;
				int count = 0;

				for (int z = 0; z < dim; z++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });
					// When it is the first field the row the player to count is
					// set.
					// It does not matter which player is counted, as the row is
					// not valid i two players occupy fields there.
					if (occupier != null) {
						if (player == null) {
							player = occupier;
							count++;
						} else {
							if (player != occupier) {
								count = 0;
								break;
							} else {
								count++;
							}
						}
					}

				}

				if (count != 0) {
					if (player == this)
						result[count - 1]++;
					else
						result[count + 4]++;
				}
			}
		}
		return result;
	}

	private int[] checkDiagX(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();

		// Traverse board in diagonal z direction
		for (int y = 0; y < dim; y++) {
			IPlayer player = null;
			int count = 0;

			for (int x = 0; x < dim; x++) {

				// Get player occupying field
				IPlayer occupier = board.getFieldValue(new int[] { x, y, x });
				// When it is the first field the row the player to count is
				// set.
				// It does not matter which player is counted, as the row is not
				// valid i two players occupy fields there.
				if (occupier != null) {
					if (player == null) {
						player = occupier;
						count++;
					} else {
						if (player != occupier) {
							count = 0;
							break;
						} else {
							count++;
						}
					}
				}

			}

			if (count != 0) {
				if (player == this)
					result[count - 1]++;
				else
					result[count + 4]++;
			}
		}
		return result;
	}

	private int[] checkDiagY(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();

		// Traverse board in diagonal z direction
		for (int z = 0; z < dim; z++) {
			IPlayer player = null;
			int count = 0;

			for (int y = 0; y < dim; y++) {

				// Get player occupying field
				IPlayer occupier = board.getFieldValue(new int[] { y, y, z });
				// When it is the first field the row the player to count is
				// set.
				// It does not matter which player is counted, as the row is not
				// valid i two players occupy fields there.
				if (occupier != null) {
					if (player == null) {
						player = occupier;
						count++;
					} else {
						if (player != occupier) {
							count = 0;
							break;
						} else {
							count++;
						}
					}
				}

			}

			if (count != 0) {
				if (player == this)
					result[count - 1]++;
				else
					result[count + 4]++;
			}
		}
		return result;
	}

	private int[] checkDiagZ(IBoard board) {
		int[] result = new int[10];
		int dim = board.getSize();

		// Traverse board in diagonal z direction
		for (int x = 0; x < dim; x++) {
			IPlayer player = null;
			int count = 0;

			for (int z = 0; z < dim; z++) {

				// Get player occupying field
				IPlayer occupier = board.getFieldValue(new int[] { x, z, z });
				// When it is the first field the row the player to count is
				// set.
				// It does not matter which player is counted, as the row is not
				// valid i two players occupy fields there.
				if (occupier != null) {
					if (player == null) {
						player = occupier;
						count++;
					} else {
						if (player != occupier) {
							count = 0;
							break;
						} else {
							count++;
						}
					}
				}

			}

			if (count != 0) {
				if (player == this)
					result[count - 1]++;
				else
					result[count + 4]++;
			}
		}
		return result;
	}
	
	private int[] check3D(IBoard board) {
		int[] result0 = check3D_0(board);
		int[] result1 = check3D_1(board);
		int[] result2 = check3D_2(board);
		int[] result3 = check3D_3(board);
		
		int[] result = new int[10];
		
		for (int i = 0; i < 10; i++)
			result[i] = result0[i] + result1[i] + result2[i] + result3[i];
		
		return result;
	}

	private int[] check3D_0(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int[] result = new int[10];
		
		for(int i = 0; i < 5; i++){

			IPlayer occupier = board.getFieldValue(new int[] { i, i, i });

			if (occupier != null) {
				if (player == null) {
					player = occupier;
					count++;
				} else {
					if (player != occupier) {
						count = 0;
						break;
					} else {
						count++;
					}
				}
			}
		}
		if (count != 0) {
			if (player == this)
				result[count - 1]++;
			else
				result[count + 4]++;
		}
		return result;
	}
	private int[] check3D_1(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int[] result = new int[10];
		
		for(int i = 0; i < 5; i++){

			IPlayer occupier = board.getFieldValue(new int[] { 4-i, i, i });

			if (occupier != null) {
				if (player == null) {
					player = occupier;
					count++;
				} else {
					if (player != occupier) {
						count = 0;
						break;
					} else {
						count++;
					}
				}
			}
		}
		if (count != 0) {
			if (player == this)
				result[count - 1]++;
			else
				result[count + 4]++;
		}
		return result;
	}
	private int[] check3D_2(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int[] result = new int[10];
		
		for(int i = 0; i < 5; i++){

			IPlayer occupier = board.getFieldValue(new int[] { i, 4-i, i });

			if (occupier != null) {
				if (player == null) {
					player = occupier;
					count++;
				} else {
					if (player != occupier) {
						count = 0;
						break;
					} else {
						count++;
					}
				}
			}
		}
		if (count != 0) {
			if (player == this)
				result[count - 1]++;
			else
				result[count + 4]++;
		}
		return result;
	}
	private int[] check3D_3(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int[] result = new int[10];
		
		for(int i = 0; i < 5; i++){

			IPlayer occupier = board.getFieldValue(new int[] { i, i, 4-i });

			if (occupier != null) {
				if (player == null) {
					player = occupier;
					count++;
				} else {
					if (player != occupier) {
						count = 0;
						break;
					} else {
						count++;
					}
				}
			}
		}
		if (count != 0) {
			if (player == this)
				result[count - 1]++;
			else
				result[count + 4]++;
		}
		return result;
	}

	private boolean initWeights() {
		for (int i = 0; i < 10; i++) {
			weights[i] = 0;
		}

		return true;
	}

}
