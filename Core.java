import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.imageio.ImageIO;

public class Core
{
	// used for storing maze data.
	public static final byte ERROR = 0;
	public static final byte UP = 1;
	public static final byte DOWN = 2;
	public static final byte LEFT = 4;
	public static final byte RIGHT = 8;

	public static final byte UP_RET = -127;
	public static final byte DOWN_RET = 66;
	public static final byte LEFT_RET = 36;
	public static final byte RIGHT_RET = 24;

	public static byte[][] maze;
	public static int x = 0, y = 0, size = 10;// position
	public static BufferedImage img;

	public static Stack<OrderedPair> solution = new Stack<OrderedPair>();

	public static void main(String args[])
	{
		long time = System.currentTimeMillis();
		int width = 10, height = 10;
		maze = setup(width, height);

		solution.push(new OrderedPair(0, 0)); // stores solution path, recorded as maze generates

		boolean solutionFound = false;

		do
		{
			byte[] dirs = getDirs(); // get list of possible directions for the maze to go
			if (dirs == null)// if array is null then we're out of bounds
			{
				System.out.println("Invalid position in maze: " + x + ", " + y);
				break;
			}

			if (dirs.length != 0)// if we can go somewhere, pick a random one
			{
				int i = (int) (Math.random() * dirs.length);
				byte dir = dirs[i];
				maze[x][y] |= dir;// record the direction into the maze state

				// find the opposite direction so that we can record it in the new cell
				byte retDir = ERROR;
				if (dir == UP)
				{
					y--;
					retDir |= DOWN_RET;
				}
				if (dir == DOWN)
				{
					y++;
					retDir |= UP_RET;
				}
				if (dir == LEFT)
				{
					x--;
					retDir |= RIGHT_RET;
				}
				if (dir == RIGHT)
				{
					x++;
					retDir |= LEFT_RET;
				}
				if (retDir == ERROR)
				{
					System.out.println("DIRECTION ERROR");
					break;
				}

				if (!solutionFound)// record solution path
					solution.push(new OrderedPair(x, y));
				if (x == maze.length - 1 && y == maze[0].length - 1)
					solutionFound = true;

				maze[x][y] |= retDir;
			} else// if no available directions then backtrack one cell
			{
				if (!solutionFound)
					solution.pop();
				if ((maze[x][y] & UP_RET) == UP_RET)
					y--;
				else if ((maze[x][y] & DOWN_RET) == DOWN_RET)
					y++;
				else if ((maze[x][y] & LEFT_RET) == LEFT_RET)
					x--;
				else if ((maze[x][y] & RIGHT_RET) == RIGHT_RET)
					x++;
				else
				{
					System.out.println("BACKTRACKING ERROR");
					break;
				}
			}
		} while (x != 0 || y != 0);// the maze will be complete when it backtracks to x=0,y=0

		// calculate and display the time it took to generate
		time = System.currentTimeMillis() - time;
		System.out.println(width + " x " + height + " maze generated in " + ((double) time / 1000.0) + " seconds.");

		draw();
	}

	public static byte[] getDirs()
	{
		ArrayList<Byte> dirs = new ArrayList<Byte>();

		if (x < 0 || x > maze.length || y < 0 || y > maze[0].length)
			return null;

		if (x > 0 && !hasReturnVal(maze[x - 1][y]))
			dirs.add(LEFT);

		if (y > 0 && !hasReturnVal(maze[x][y - 1]))
			dirs.add(UP);

		if (x < maze.length - 1 && !hasReturnVal(maze[x + 1][y]))
			dirs.add(RIGHT);

		if (y < maze[0].length - 1 && !hasReturnVal(maze[x][y + 1]))
			dirs.add(DOWN);

		byte[] array = new byte[dirs.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = dirs.get(i);
		return array;
	}

	public static boolean hasReturnVal(byte b)
	{
		return (b & UP_RET) == UP_RET || (b & DOWN_RET) == DOWN_RET || (b & LEFT_RET) == LEFT_RET || (b & RIGHT_RET) == RIGHT_RET;
	}

	public static void draw()
	{
		if (img == null)
			img = new BufferedImage(maze.length * size, maze[0].length * size, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = img.createGraphics();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, maze.length * size, maze[0].length * size);

		g.setColor(Color.BLACK);
		for (int i = 0; i < maze.length; i += 1)
			for (int j = 0; j < maze[i].length; j += 1)
			{
				if ((maze[i][j] & UP) == 0)
					g.drawLine(i * size, j * size, i * size + size, j * size);

				if ((maze[i][j] & DOWN) == 0)
					g.drawLine(i * size, clamp(0, maze[i].length * size - 1, j * size + size), i * size + size, clamp(0, maze[i].length * size - 1, j * size + size));

				if ((maze[i][j] & LEFT) == 0)
					g.drawLine(i * size, j * size, i * size, j * size + size);

				if ((maze[i][j] & RIGHT) == 0)
					g.drawLine(clamp(0, maze.length * size - 1, i * size + size), j * size, clamp(0, maze.length * size - 1, i * size + size), j * size + size);
			}

		int colorChange = (int) Math.floor(255.0 / solution.size());
		int red = 0;
		while (solution.size() > 0)
		{
			g.setColor(new Color(red, 0, 0));
			red += colorChange;
			OrderedPair o = solution.pop();
			g.fillRect(o.x * size + 1, o.y * size + 1, size - 2, size - 2);
		}

		g.dispose();

		File f = new File(System.getProperty("user.home") + "\\Desktop\\maze.png");
		for (int i = 1; f.exists(); i++)
			f = new File(System.getProperty("user.home") + "\\Desktop\\maze" + i + ".png");
		try
		{
			ImageIO.write(img, "png", f);
			Desktop.getDesktop().edit(f);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Clamps a number between two values.
	 * 
	 * @param l
	 *            the lower bound to clamp to.
	 * @param h
	 *            the upper bound to clamp to.
	 * @param i
	 *            the integer to clamp.
	 * @return an integer between the lower and upper bounds provided.
	 */
	public static int clamp(int l, int h, int i)
	{
		return i < l ? l : i > h ? h : i;
	}

	/**
	 * Creates the array to store maze data and initialized the edges to be walls.
	 * 
	 * @param w
	 *            width of maze in cells.
	 * @param h
	 *            height of maze in cells.
	 * @return a 2-dimensional array.
	 */
	public static byte[][] setup(int w, int h)
	{
		byte[][] grid = new byte[w][h];

		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++)
			{
				grid[i][0] |= UP;
				grid[i][h - 1] |= DOWN;
				grid[0][j] |= LEFT;
				grid[w - 1][j] |= RIGHT;
			}

		grid[x][y] |= UP_RET;
		return grid;
	}
}

class OrderedPair
{
	public int x, y;

	public OrderedPair(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}
