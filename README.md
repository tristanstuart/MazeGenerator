# MazeGenerator
This program was a personal project created with the intention of better understanding bitwise operations. I have constructed versions of this maze generator before using a similar algorithm, but the storage of the data was always more verbose than just a 2-dimensional array of bytes. The intention here was to explore the idea of compacting the data of the maze down into as little space as possible during generation.

I dont recall the name of this maze algorithm, but it can be described in a few simple steps:
1. Examine all adjacent squares for if they have been visited or not.
2. Pick a random unvisited adjacent square and move there, recording your path.
3. If there are no unvisited adjacent squares, backtrack to the previously visited square.

Repeat steps 1-3 until the current square returns back to the starting square.

The maze is output as an image titled maze#.png to the user's desktop directory with the solution path highlighted. The ```public static int size``` variable at the top of the file controls the square size of the maze in block units. The program will output the time it took to generate the maze in seconds when completed.
