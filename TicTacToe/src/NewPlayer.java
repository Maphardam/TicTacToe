import de.ovgu.dke.teaching.ml.tictactoe.api.IBoard;
import de.ovgu.dke.teaching.ml.tictactoe.api.IMove;
import de.ovgu.dke.teaching.ml.tictactoe.api.IPlayer;
import de.ovgu.dke.teaching.ml.tictactoe.api.IllegalMoveException;
import de.ovgu.dke.teaching.ml.tictactoe.game.Move;

import java.util.List;
import java.util.Arrays;

/**
 * Implementation of a Least-Mean-Squares (LMS) algorithm for a 5x5x5
 * Tic-Tac-Toe game
 * 
 * @author Tilman Krokotsch, 199917
 * @author Tim Sabsch, 200088
 * 
 * date: 11-03-2014
 */
public class NewPlayer implements IPlayer {

	// learning rate
	private static final double ETA = 0.001;
	// weights we will adjust after each game
	private double[] weights = { 1, 1, 1, 1, 1, 1, 1, 1 };
	// number of features we use
	private final int FEATURE_LENGTH = 8;
	// are we in a learning phase?
	private boolean learning = true;

	public String getName() {
		return "EXTERMINATE!";
	}

	public int[] makeMove(IBoard board) {

		// create a clone of the board that can be modified
		IBoard copy = board.clone();

		int[] movePos = decideBestMove(copy);
		// do a move using the cloned board
		try {
			// System.out.println(this + " " + Arrays.toString(movePos));
			copy.makeMove(new Move(this, movePos));
		} catch (IllegalMoveException e) {
			// move was not allowed
		}

		// return your final decision for your next move
		return movePos;
	}

	public void onMatchEnds(IBoard board) {
		if (learning) {
			updateWeights(board);
			System.out.println(Arrays.toString(weights));
		} else {

		}
		return;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// ***********************************OWN**FUNCTIONS***************************************//
	// //////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * We use the following features: 
	 * x0 = Single markers in a line of our player 
	 * x1 = 2 marker in a line of our player 
	 * x2 = 3 marker in a line of our player 
	 * x3 = 4 marker in a line of our player 
	 * x4 = Single marker in a line of the enemy player 
	 * x5 = 2 marker in a line of the enemy player 
	 * x6 = 3 marker in a line of the enemy player 
	 * x7 = 4 marker in a line of the enemy player
	 */

	/**
	 * determines the best possible move
	 * 
	 * @param board
	 *            the current board state
	 * @return the best possible move
	 */
	private int[] decideBestMove(IBoard board) {
		int dimSize = board.getSize();
		double maxValue = -100;
		int[] bestMove = null;

		for (int x = 0; x < dimSize; x++) {
			for (int y = 0; y < dimSize; y++) {
				for (int z = 0; z < dimSize; z++) {

					// if field is already used, we cant set a marker on it
					// TODO: may be redundant. @see lines 149-155
					if (board.getFieldValue(new int[] { x, y, z }) != null)
						continue;

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

	/**
	 * computes V(x1,x2,...,x8) = w1*x1 + w2*x2 + ... + w8*x8
	 * 
	 * @param features
	 *            the x values (which are representing the different features)
	 * @return the result of V(x1,...,x8)
	 */
	private double computeValue(int[] features) {
		double sum = 0;
		for (int i = 0; i < FEATURE_LENGTH; i++) {
			sum += features[i] * weights[i];
		}
		return sum;
	}

	/**
	 * computes the current feature values. Then it computes the feature values
	 * we would obtain, if we make a move at position {x,y,z}.
	 * 
	 * @param board
	 *            the current board state
	 * @param x
	 *            the x position we inspect currently
	 * @param y
	 *            the y position we inspect currently
	 * @param z
	 *            the z position we inspect currently
	 * @return the difference between the current feature sum and the future
	 *         feature sum
	 */
	private double computeValueAtPosition(IBoard board, int x, int y, int z) {

		IBoard copy = board.clone();

		int[] currentFeatures = getFeatures(copy);
		double currentSum = computeValue(currentFeatures);

		try {
			copy.makeMove(new Move(this, new int[] { x, y, z }));
		} catch (IllegalMoveException e) {
			// return any negative value to ensure, that this move won't be
			// choosen
			return -101;
		}

		int[] futureFeatures = getFeatures(copy);
		double futureSum = computeValue(futureFeatures);

		return futureSum - currentSum;
	}

	/**
	 * checks all possible lines and returns a feature list of this form: [ x1,
	 * x2, x3, ..., x8], where x1-x4 are the number of unblocked
	 * single/double/... markers in a line of our own player and x5-x8 the
	 * numbers of unblocked single/double/... markers in a line of the enemy
	 * 
	 * @param board
	 *            a board state
	 * @return an array containing the feature list of the board state
	 */
	private int[] getFeatures(IBoard board) {
		int[] xLines = checkX(board);
		int[] yLines = checkY(board);
		int[] zLines = checkZ(board);
		int[] diagXLines = checkDiagX(board);
		int[] diagYLines = checkDiagY(board);
		int[] diagZLines = checkDiagZ(board);
		int[] diag3DLines = check3D(board);

		int[] features = new int[FEATURE_LENGTH];

		for (int i = 0; i < FEATURE_LENGTH; i++) {
			features[i] += xLines[i] + yLines[i] + zLines[i] + diagXLines[i]
					+ diagYLines[i] + diagZLines[i] + diag3DLines[i];
		}
		return features;
	}

	private int[] checkX(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();
		// Traverse board in horizontal x direction
		for (int z = 0; z < dimSize; z++) {
			for (int y = 0; y < dimSize; y++) {
				IPlayer player = null;
				int count = 0;

				for (int x = 0; x < dimSize; x++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });

					// check the occupier
					try {
						player = checkFieldOccupier(occupier, player);
						count++;
					} catch (InvalidLineException e) {
						count = 0;
						break;
					} catch (NotOccupiedException e) {
					}

				}
				// update the result
				result = updateFeatureResult(player, result, count);
			}
		}
		return result;
	}

	private int[] checkY(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();

		// Traverse board in horizontal y direction
		for (int x = 0; x < dimSize; x++) {
			for (int z = 0; z < dimSize; z++) {
				IPlayer player = null;
				int count = 0;

				for (int y = 0; y < dimSize; y++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });

					try {
						player = checkFieldOccupier(occupier, player);
						count++;
					} catch (InvalidLineException e) {
						count = 0;
						break;
					} catch (NotOccupiedException e) {
					}

				}

				result = updateFeatureResult(player, result, count);
			}
		}

		return result;
	}

	private int[] checkZ(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();

		// Traverse board in horizontal z direction
		for (int x = 0; x < dimSize; x++) {
			for (int y = 0; y < dimSize; y++) {
				IPlayer player = null;
				int count = 0;

				for (int z = 0; z < dimSize; z++) {

					// Get player occupying field
					IPlayer occupier = board
							.getFieldValue(new int[] { x, y, z });

					try {
						player = checkFieldOccupier(occupier, player);
						count++;
					} catch (InvalidLineException e) {
						count = 0;
						break;
					} catch (NotOccupiedException e) {
					}

				}

				result = updateFeatureResult(player, result, count);
			}
		}
		return result;
	}

	private int[] checkDiagX(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();

		// Traverse board in diagonal x direction
		for (int y = 0; y < dimSize; y++) {
			IPlayer player_1 = null;
			IPlayer player_2 = null;
			int count_1 = 0;
			int count_2 = 0;
			boolean isBlocked_1 = false;
			boolean isBlocked_2 = false;

			for (int x = 0; x < dimSize; x++) {

				/*
				 * check both diagonal options:
				 * 
				 * x o o o o x o x o o x o o o x x o o
				 */

				if (!isBlocked_1) {
					IPlayer occupier_1 = board.getFieldValue(new int[] { x, y,
							x });
					try {
						player_1 = checkFieldOccupier(occupier_1, player_1);
						count_1++;
					} catch (InvalidLineException e) {
						count_1 = 0;
						isBlocked_1 = true;
					} catch (NotOccupiedException e) {
					}
				}

				if (!isBlocked_2) {
					IPlayer occupier_2 = board.getFieldValue(new int[] { x, y,
							(dimSize - 1) - x });
					try {
						player_2 = checkFieldOccupier(occupier_2, player_2);
						count_2++;
					} catch (InvalidLineException e) {
						count_2 = 0;
						isBlocked_2 = true;
					} catch (NotOccupiedException e) {
					}
				}

			}

			result = updateFeatureResult(player_1, result, count_1);
			result = updateFeatureResult(player_2, result, count_2);
		}
		return result;
	}

	private int[] checkDiagY(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();

		// Traverse board in diagonal y direction
		for (int z = 0; z < dimSize; z++) {
			IPlayer player_1 = null;
			IPlayer player_2 = null;
			int count_1 = 0;
			int count_2 = 0;
			boolean isBlocked_1 = false;
			boolean isBlocked_2 = false;

			for (int y = 0; y < dimSize; y++) {

				if (!isBlocked_1) {
					IPlayer occupier_1 = board.getFieldValue(new int[] { y, y,
							z });
					try {
						player_1 = checkFieldOccupier(occupier_1, player_1);
						count_1++;
					} catch (InvalidLineException e) {
						count_1 = 0;
						isBlocked_1 = true;
					} catch (NotOccupiedException e) {
					}
				}

				if (!isBlocked_2) {
					IPlayer occupier_2 = board.getFieldValue(new int[] { y,
							(dimSize - 1) - y, z });
					try {
						player_2 = checkFieldOccupier(occupier_2, player_2);
						count_2++;
					} catch (InvalidLineException e) {
						count_2 = 0;
						isBlocked_2 = true;
					} catch (NotOccupiedException e) {
					}
				}

			}

			result = updateFeatureResult(player_1, result, count_1);
			result = updateFeatureResult(player_2, result, count_2);
		}
		return result;
	}

	private int[] checkDiagZ(IBoard board) {
		int[] result = new int[FEATURE_LENGTH];
		int dimSize = board.getSize();

		// Traverse board in diagonal z direction
		for (int x = 0; x < dimSize; x++) {
			IPlayer player_1 = null;
			IPlayer player_2 = null;
			int count_1 = 0;
			int count_2 = 0;
			boolean isBlocked_1 = false;
			boolean isBlocked_2 = false;

			for (int z = 0; z < dimSize; z++) {

				if (!isBlocked_1) {
					IPlayer occupier_1 = board.getFieldValue(new int[] { x, z,
							z });
					try {
						player_1 = checkFieldOccupier(occupier_1, player_1);
						count_1++;
					} catch (InvalidLineException e) {
						count_1 = 0;
						isBlocked_1 = true;
					} catch (NotOccupiedException e) {
					}
				}

				if (!isBlocked_2) {
					IPlayer occupier_2 = board.getFieldValue(new int[] { x,
							(dimSize - 1) - z, z });
					try {
						player_2 = checkFieldOccupier(occupier_2, player_2);
						count_2++;
					} catch (InvalidLineException e) {
						count_2 = 0;
						isBlocked_2 = true;
					} catch (NotOccupiedException e) {
					}
				}

			}

			result = updateFeatureResult(player_1, result, count_1);
			result = updateFeatureResult(player_2, result, count_2);
		}
		return result;
	}

	private int[] check3D(IBoard board) {
		int[] result0 = check3D_0(board);
		int[] result1 = check3D_1(board);
		int[] result2 = check3D_2(board);
		int[] result3 = check3D_3(board);

		int[] result = new int[FEATURE_LENGTH];

		for (int i = 0; i < FEATURE_LENGTH; i++)
			result[i] = result0[i] + result1[i] + result2[i] + result3[i];

		return result;
	}

	private int[] check3D_0(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int dimSize = board.getSize();
		int[] result = new int[FEATURE_LENGTH];

		for (int i = 0; i < dimSize; i++) {

			IPlayer occupier = board.getFieldValue(new int[] { i, i, i });

			try {
				player = checkFieldOccupier(occupier, player);
				count++;
			} catch (InvalidLineException e) {
				count = 0;
				break;
			} catch (NotOccupiedException e) {
			}
		}
		result = updateFeatureResult(player, result, count);
		return result;
	}

	private int[] check3D_1(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int dimSize = board.getSize();
		int[] result = new int[FEATURE_LENGTH];

		for (int i = 0; i < dimSize; i++) {

			IPlayer occupier = board.getFieldValue(new int[] {
					(dimSize - 1) - i, i, i });

			try {
				player = checkFieldOccupier(occupier, player);
				count++;
			} catch (InvalidLineException e) {
				count = 0;
				break;
			} catch (NotOccupiedException e) {
			}
		}
		result = updateFeatureResult(player, result, count);
		return result;
	}

	private int[] check3D_2(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int dimSize = board.getSize();
		int[] result = new int[FEATURE_LENGTH];

		for (int i = 0; i < dimSize; i++) {

			IPlayer occupier = board.getFieldValue(new int[] { i,
					(dimSize - 1) - i, i });

			try {
				player = checkFieldOccupier(occupier, player);
				count++;
			} catch (InvalidLineException e) {
				count = 0;
				break;
			} catch (NotOccupiedException e) {
			}
		}
		result = updateFeatureResult(player, result, count);
		return result;
	}

	private int[] check3D_3(IBoard board) {
		IPlayer player = null;
		int count = 0;
		int dimSize = board.getSize();
		int[] result = new int[FEATURE_LENGTH];

		for (int i = 0; i < dimSize; i++) {

			IPlayer occupier = board.getFieldValue(new int[] { i, i,
					(dimSize - 1) - i });

			try {
				player = checkFieldOccupier(occupier, player);
				count++;
			} catch (InvalidLineException e) {
				count = 0;
				break;
			} catch (NotOccupiedException e) {
			}
		}
		result = updateFeatureResult(player, result, count);
		return result;
	}

	/**
	 * updates the feature list using the number of markers in a line and the
	 * occupying player
	 * 
	 * @param player
	 *            the occupying player
	 * @param result
	 *            the current feature list
	 * @param count
	 *            the number of markers in a line
	 * @return the updated feature list
	 */

	private int[] updateFeatureResult(IPlayer player, int[] result, int count) {

		if (count != 0) {
			if (player == null) {
				// TODO: I don't think, this will happen anytime
			} else if (player == this)
				result[count - 1]++;
			else {
				result[count + (FEATURE_LENGTH / 2 - 1)]++;
			}
		}
		return result;
	}

	/**
	 * checks, which player occupies the current field
	 * 
	 * @param occupier
	 *            the current occupier of the field
	 * @param player
	 *            the former line holder
	 * @return the (new) line holder
	 * @throws InvalidLineException
	 *             if the line contains marker of two different player
	 * @throws NotOccupiedException
	 *             if the field is not occupied
	 */
	private IPlayer checkFieldOccupier(IPlayer occupier, IPlayer player)
			throws InvalidLineException, NotOccupiedException {

		if (occupier != null) {
			// if no occupier and no player defined, set player as the occupier
			if (player == null) {
				player = occupier;
			} else {
				// if two different player have stones in the line, the line is
				// invalid
				if (player != occupier)
					throw new InvalidLineException();
			}
		} else
			throw new NotOccupiedException();
		return player;
	}

	/**
	 * updates the weights using the formula wi = wi + eta * xi * error for each
	 * step of the game, where eta is the learning rate, xi are the different
	 * features and error is the difference between the training example value
	 * and the learned value The training example value is set as the successive
	 * learning value. To get the feature example of each step, we reconstruct
	 * the board state and compute the features.
	 * 
	 * @param board
	 *            the final board state
	 */
	private void updateWeights(IBoard board) {

		IBoard boardCopy = board.clone();

		// get a list of all moves done
		List<IMove> history = board.getMoveHistory();

		/*
		 * for error computation, we need the learned V and the V of our
		 * training example. if we are in a final state, our vTrain will be 0
		 * (draw) or +100/-100. As we go backward through the move list, this
		 * will happen the first time. Otherwise we set vTrain(b) =
		 * learnedV(b-1)
		 */
		double vLearned = 0;
		double vTrain = 0;

		IPlayer winner = board.getWinner();
		if (winner == null) {
		} else if (winner == this)
			vTrain = 100;
		else
			vTrain = -100;

		while (!history.isEmpty()) {

			// we only want to consider our own moves
			if (history.get(history.size() - 1).getPlayer() != this) {
				history.remove(history.size() - 1);
				if (history.isEmpty())
					break;
			}
			boardCopy.clear();
			IBoard reconstuctedBoard = reconstructFeatures(boardCopy, history);
			int[] features = getFeatures(reconstuctedBoard);

			vLearned = computeValue(features);

			double error = vTrain - vLearned;

			for (int i = 0; i < FEATURE_LENGTH; i++) {
				weights[i] = weights[i] + ETA * features[i] * error;
			}

			vTrain = vLearned;

			history.remove(history.size() - 1);
		}
	}

	/**
	 * reconstructs the board state using a movement history
	 * 
	 * @param board
	 *            a board state
	 * @param history
	 *            a movement list
	 * @return a board containing the moves of history
	 */
	private IBoard reconstructFeatures(IBoard board, List<IMove> history) {

		IBoard copy = board.clone();
		copy.clear();
		for (IMove move : history) {
			try {
				copy.makeMove(move);
			} catch (IllegalMoveException e) {
			}
		}
		return copy;
	}

	private class InvalidLineException extends Exception {
		private static final long serialVersionUID = 3669759689830266773L;
	}

	private class NotOccupiedException extends Exception {
		private static final long serialVersionUID = 6092945789420077155L;
	}
}
