# ComboLockPath

To do list:

1) Create better mutations - now we perform them blind - it would be better to store additional information so that you could reason about likely effect of changes, and therefore, reduce the number of mutations per child.

2) Current strategy implements elitism - may not be best

3) Think about restrictions on lock configuration to prevent casual observer from finding word, e.g.:
- if word is in the starting lock position, we will not use it
- if word can be read in a straight line at any offset in starting position, we will not use it
- if word can be read by moving at most one position up or down in starting position, we will not use it.  For example, consider the lock below, with the following rows visible, we would not use dogs:
DABC
AOBS
ABGC
- right now only restriction is that the min distance is at least 6 (meaning that two wheels need to be turned).  This restriction could be removed when implementing the previous ones.

To run the program, you need to provide the following input via standard input.  On a line by itself, you enter n, the number of wheels.  Then the next n lines should contain the letters on each wheel as a string, e.g.:

4

ABCDEFGHIJ

ABCDEFGHIJ

ABCDEFGHIJ

ABCD FGHIJ
