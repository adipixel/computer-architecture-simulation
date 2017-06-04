----------------------------------------------------------------------------
Folder contains following files:
----------------------------------------------------------------------------
Predictor.java - Source Code File
sample_branch_sequence.txt -  this is input branch sequence and is the first argument in the run command.
sample_branch_output_full.txt - Used to compare with the output

----------------------------------------------------------------------------
Instructions to run
----------------------------------------------------------------------------
execute the following commands:

make
example:
bingsuns2% make

java Predictor <input_branch_sequence.txt> output_filename.txt
example:
bingsuns2% java Predictor sample_branch_sequence.txt output.txt


----------------------------------------------------------------------------
To check difference
----------------------------------------------------------------------------
diff --ignore-all-space sample_branch_output_full.txt outputfile

example: 
bingsuns2% diff --ignore-all-space sample_branch_output_full.txt output.txt


----------------------------------------------------------------------------
To clean up class file
----------------------------------------------------------------------------
make clean
