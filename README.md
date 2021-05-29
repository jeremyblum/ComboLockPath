# ComboLockPath

To do list:

1) Create better mutations - now we perform them blind - it would be better to store additional information so that you could reason about likely effect of changes, and therefore, reduce the number of mutations per child.

2) Current strategy implements elitism - may not be best


To run the program, you need to provide the following input via standard input.  On a line by itself, you enter n, the number of wheels.  Then the next n lines should contain the letters on each wheel as a string, e.g.:

4

ABCDEFGHIJ

ABCDEFGHIJ

ABCDEFGHIJ

ABCD FGHIJ
